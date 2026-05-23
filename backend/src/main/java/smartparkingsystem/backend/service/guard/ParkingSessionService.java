package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.dto.request.IncidentRequest;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.*;
import smartparkingsystem.backend.dto.response.ai.AiDetectionResult;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.dto.response.parkingSession.ParkingSessionResponse;
import smartparkingsystem.backend.entity.*;
import smartparkingsystem.backend.entity.type.*;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.ParkingSessionMapper;
import smartparkingsystem.backend.repository.*;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.service.calculator.FeeCalculationFactory;
import smartparkingsystem.backend.service.calculator.FeeCalculationStrategy;
import smartparkingsystem.backend.service.thirdService.*;

import java.math.BigInteger;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParkingSessionService {
    public record FeeCalculationResult(BigInteger totalFee, List<UUID> relatedSessionIds) {}
    private final AiIntegrationService aiIntegrationService;
    private final ParkingSessionRepository parkingSessionRepository;
    private final LaneRepository laneRepository;
    private final FeeCalculationFactory feeCalculationFactory;
    private final PricingRuleRepository pricingRuleRepository;
    private final InvoiceService invoiceService;
    private final UserService userService;
    private final InvoiceRepository invoiceRepository;
    private final GuardIncidentService guardIncidentService;
    private final ParkingSessionMapper parkingSessionMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final FileService fileService;

    private CheckInResponse processCheckInForBicycle(CheckInRequest request, String imageUrl) {
        return parkingSessionMapper.toCheckInResponse("BICYCLE", imageUrl, 1.0f, request.getVehicleType());
    }
    @Transactional
    public CheckInResponse processCheckIn(CheckInRequest request, MultipartFile image) {

        String imageUrl = fileService.storeImage(image, "check-in", "Không thể lưu ảnh check-in");
        if(request.getVehicleType() == VehicleTypeEnum.BICYCLE){
            return processCheckInForBicycle(request, imageUrl);
        }
        AiDetectionResult aiResult = aiIntegrationService.getDetectionResultFromAi(fileService.buildAbsoluteImagePath(imageUrl));
        String licensePlate = aiResult.getPlateNumber();
        return parkingSessionMapper.toCheckInResponse(licensePlate, imageUrl, fileService.confidenceOrRandom(aiResult.getConfidence()), request.getVehicleType());
    }
    @Transactional
    public void cancelCheckIn(String imageUrl) {
        fileService.deleteImage(imageUrl);
    }
    @Transactional
    public ParkingSessionResponse processConfirmCheckIn(ConfirmCheckInRequest request) {
        if(request.getVehicleType() != VehicleTypeEnum.BICYCLE) {
            parkingSessionRepository.findFirstByStatusAndFinalPlateIgnoreCase(SessionStatus.PARKED, request.getFinalPlate())
                    .ifPresent((existingSession) -> {
                        throw new DuplicateResourceException("Đã tồn tại phiên đỗ xe mở với biển số: " + request.getFinalPlate());
                    });
        }
        Lane lane = laneRepository.findById(request.getEntryLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getEntryLaneId()));
        boolean isMonth;
        if(request.getVehicleType() == VehicleTypeEnum.BICYCLE) {
            isMonth = false;
        } else {
            isMonth = subscriptionRepository.existsByVehicle_LicensePlateAndStatus(request.getFinalPlate(), SubStatus.ACTIVE);
        }

        ParkingSession session = parkingSessionMapper.toEntityForConfirmCheckIn(request, lane, isMonth);
        session = parkingSessionRepository.save(session);
        if (!request.getPlateInOcr().equals(request.getFinalPlate())) {
            guardIncidentService.reportIncident(session, "Biển số xác nhận không khớp với biển số OCR", IncidentTypeEnum.WRONG_PLATE, request.getImageInUrl());
        }

        return parkingSessionMapper.toParkingSessionResponse(session);
    }

    private CheckOutResponse processCheckOutForBicycle(ParkingSession session, Lane lane, String imageUrl) {
        BigInteger fee = calculateFee(session);
        return parkingSessionMapper.toCheckOutResponse(session, fee, BigInteger.ZERO, null, lane, imageUrl, 1.0f, "BICYCLE");
    }
    @Transactional
    public CheckOutResponse processCheckOut(CheckOutRequest request, MultipartFile image) {
        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));
        ParkingSession session = parkingSessionRepository.findFirstByIdAndStatus(request.getParkingSessionId(), SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với id: " + request.getParkingSessionId()));
        String imageUrl = fileService.storeImage(image, "check-out", "Không thể lưu ảnh check-out");
        if(session.getVehicleType() == VehicleTypeEnum.BICYCLE) {
            return processCheckOutForBicycle(session, lane, imageUrl);
        }

        AiDetectionResult aiResult = aiIntegrationService.getDetectionResultFromAi(fileService.buildAbsoluteImagePath(imageUrl));
        String licensePlate = aiResult.getPlateNumber();


        BigInteger fee = calculateFee(session);
        List<UUID> relatedSessionIds = null;
        if (session.getFinalPlate().equals(licensePlate)) {
            FeeCalculationResult result = getTotalFee(session, fee);
            relatedSessionIds = result.relatedSessionIds;
            fee = fee.add(result.totalFee);
        } else {
            throw new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số: " + licensePlate);
        }
        return parkingSessionMapper.toCheckOutResponse(session, fee, BigInteger.ZERO, relatedSessionIds, lane, imageUrl, fileService.confidenceOrRandom(aiResult.getConfidence()), licensePlate);
    }
    @Transactional
    public void processConfirmCheckOut(ConfirmCheckOutRequest request) {
        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));
        ParkingSession session = parkingSessionRepository.findFirstByIdAndStatus(request.getParkingSessionId(), SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với ID: " + request.getParkingSessionId()));

        if(request.getRelatedSessionIds() != null && !request.getRelatedSessionIds().isEmpty()) {
            for(UUID relatedSessionId : request.getRelatedSessionIds()) {
                ParkingSession parkingSession = parkingSessionRepository.findById(relatedSessionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe với ID: " + relatedSessionId));
                if(relatedSessionId.equals(session.getId())) {
                    continue;
                }
                parkingSessionMapper.updateEntityForCheckOut(parkingSession, lane, request.getImageOutUrl(), request.getConfidenceOut(), parkingSession.getFinalPlate(), SessionStatus.COMPLETED, request.getTimeOut());
                parkingSessionRepository.save(parkingSession);
                invoiceRepository.findByParkingSession(parkingSession)
                        .ifPresent(invoice -> {
                            invoice.setStatus(PaymentStatus.SUCCESS);
                            invoice.setPaymentTime(request.getTimeOut());
                            invoice.setPaymentMethod(request.getPaymentMethod());
                            invoice.setCashier(userService.getCurrentUser());
                            invoiceRepository.save(invoice);
                        });
            }
        }

        parkingSessionMapper.updateEntityForCheckOut(session, lane, request.getImageOutUrl(), request.getConfidenceOut(), session.getFinalPlate(), SessionStatus.COMPLETED, request.getTimeOut());
        Invoice invoice = invoiceService.createInvoiceForParkingSession(session, request.getParkingAmount(), request.getPenaltyAmount(), request.getPaymentMethod(), userService.getCurrentUser(), PaymentStatus.SUCCESS, request.getTimeOut());
        invoiceRepository.save(invoice);
        parkingSessionRepository.save(session);
    }
    @Transactional
    public void reportGeneralIncident(IncidentRequest request, MultipartFile evidenceImage) {
        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe với ID: " + request.getParkingSessionId()));

        String evidenceUrl = fileService.storeImage(evidenceImage, "evidence", "Không thể lưu ảnh bằng chứng");
        guardIncidentService.reportIncident(session, request.getDescription(), request.getIncidentType(), evidenceUrl);
    }

    private BigInteger calculatePenalty(ParkingSession session) {
        PricingRule pricingRule = pricingRuleRepository.findByVehicleTypeAndActiveTrue(session.getVehicleType())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc giá cho loại xe: " + session.getVehicleType()));
        return pricingRule.getPenaltyFee();
    }

    private BigInteger calculateFee(ParkingSession session) {
        if (session.isMonth()) {
            return BigInteger.ZERO;
        }
        PricingRule pricingRule = pricingRuleRepository.findByVehicleTypeAndActiveTrue(session.getVehicleType())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc giá cho loại xe: " + session.getVehicleType()));
        FeeCalculationStrategy strategy = feeCalculationFactory.getCalculator(pricingRule.getStrategy());
        return strategy.calculateFee(session.getTimeIn(), LocalDateTime.now(), pricingRule);
    }
    @Transactional
    public CheckOutResponse reportLostCard(CheckOutWithoutCardRequest request, MultipartFile image, MultipartFile evidenceImage) {
        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));

        String imageUrl = fileService.storeImage(image, "check-out", "Không thể lưu ảnh check-out");

        AiDetectionResult aiResult = aiIntegrationService.getDetectionResultFromAi(fileService.buildAbsoluteImagePath(imageUrl));
        String licensePlate = aiResult.getPlateNumber();

        ParkingSession session = parkingSessionRepository.findFirstByStatusAndFinalPlateIgnoreCase(SessionStatus.PARKED, licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số xe: " + licensePlate));

        String evidenceUrl = fileService.storeImage(evidenceImage, "evidence", "Không thể lưu ảnh bằng chứng");
        guardIncidentService.reportIncident(session, request.getDescription(), IncidentTypeEnum.LOST_CARD, evidenceUrl);

        BigInteger penalty = calculatePenalty(session);
        BigInteger fee = calculateFee(session);
        FeeCalculationResult result = getTotalFee(session, fee);
        List<UUID> relatedSessionIds = result.relatedSessionIds;
        fee = fee.add(result.totalFee);
        return parkingSessionMapper.toCheckOutResponse(session, fee, penalty, relatedSessionIds, lane, imageUrl, fileService.confidenceOrRandom(aiResult.getConfidence()), licensePlate);
    }


    @Transactional
    public Page<ParkingSessionResponse> getAllParkingSessions(Pageable pageable, SessionStatus status, String licensePlate, VehicleTypeEnum vehicleType) {
        Sort sort = Sort.by("timeIn").descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ParkingSession> page;

        // Xác định repository method dựa trên sự kết hợp của các filter
        boolean hasStatus = status != null;
        boolean hasLicensePlate = licensePlate != null && !licensePlate.isBlank();
        boolean hasVehicleType = vehicleType != null;

        // Sử dụng partial search (LIKE) cho licensePlate
        if (hasLicensePlate && hasVehicleType && hasStatus) {
            // Tất cả 3 filter - sử dụng LIKE cho licensePlate
            page = parkingSessionRepository.findByFinalPlateContainingIgnoreCaseAndVehicleTypeAndStatus(
                    licensePlate, vehicleType, status, sortedPageable);
        } else if (hasLicensePlate && hasVehicleType) {
            // Có licensePlate và vehicleType - sử dụng LIKE cho licensePlate
            page = parkingSessionRepository.findByFinalPlateContainingIgnoreCaseAndVehicleType(
                    licensePlate, vehicleType, sortedPageable);
        } else if (hasLicensePlate && hasStatus) {
            // Có licensePlate và status - sử dụng LIKE cho licensePlate
            page = parkingSessionRepository.findByFinalPlateContainingIgnoreCaseAndStatus(
                    licensePlate, status, sortedPageable);
        } else if (hasVehicleType && hasStatus) {
            // Có vehicleType và status
            page = parkingSessionRepository.findByVehicleTypeAndStatus(
                    vehicleType, status, sortedPageable);
        } else if (hasLicensePlate) {
            // Chỉ có licensePlate - sử dụng LIKE
            page = parkingSessionRepository.findByFinalPlateContainingIgnoreCase(
                    licensePlate, sortedPageable);
        } else if (hasVehicleType) {
            // Chỉ có vehicleType
            page = parkingSessionRepository.findByVehicleType(
                    vehicleType, sortedPageable);
        } else if (hasStatus) {
            // Chỉ có status
            page = parkingSessionRepository.findByStatus(status, sortedPageable);
        } else {
            // Không có filter nào, lấy tất cả
            page = parkingSessionRepository.findAll(sortedPageable);
        }

        return page.map(parkingSessionMapper::toParkingSessionResponse);
    }
    @Transactional
    public Page<ParkingSessionResponse> getParkingSessionsByLicensePlate(String licensePlate, Pageable pageable) {
        Sort sort = Sort.by("timeIn").descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return parkingSessionRepository.findByFinalPlateIgnoreCase(licensePlate, sortedPageable)
                .map(parkingSessionMapper::toParkingSessionResponse);
    }

    public Long getTotalParkedVehicles(SessionStatus status) {
        return parkingSessionRepository.countByStatus(status);
    }

    public Path getParkingSessionImagePath(UUID parkingSessionId, String type) {
        ParkingSession session = parkingSessionRepository.findById(parkingSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe với ID: " + parkingSessionId));
        if (type == null || type.isBlank()) {
            throw new ValidationException("Type ảnh không được để trống");
        }
        String imageUrl;
        String normalizedType = type.trim().toLowerCase();
        if ("in".equals(normalizedType)) {
            imageUrl = session.getImageInUrl();
        } else if ("out".equals(normalizedType)) {
            imageUrl = session.getImageOutUrl();
        } else {
            throw new ValidationException("Type ảnh không hợp lệ, chỉ chấp nhận 'in' hoặc 'out'");
        }
        return fileService.getImagePath(imageUrl);
    }

    private FeeCalculationResult getTotalFee(ParkingSession session, BigInteger initialFee) {
        BigInteger currentFee = initialFee != null ? initialFee : BigInteger.ZERO;
        List<UUID> sessionIds = null;

        if (session.getRootId() != null) {
            List<ParkingSession> relatedSessions = parkingSessionRepository.findAllByRootId(session.getRootId());
            sessionIds = relatedSessions.stream()
                    .map(ParkingSession::getId)
                    .toList();
            for (ParkingSession relatedSession : relatedSessions) {
                if (!relatedSession.getId().equals(session.getId())) {
                    currentFee = currentFee.add(calculateFee(relatedSession));
                }
            }
        }
        return new FeeCalculationResult(currentFee, sessionIds);
    }

    public void handleSubscriptionChanging(Subscription sub, SubStatus newStatus) {
        ParkingSession session = parkingSessionRepository.findFirstByStatusAndFinalPlateIgnoreCase(SessionStatus.PARKED, sub.getVehicle().getLicensePlate())
                .orElse(null);
        if (session == null) {
            return;
        }
        if(session.getRootId() == null){
            session.setRootId(UUID.randomUUID());
        }
        session.setStatus(SessionStatus.COMPLETED);
        parkingSessionRepository.save(session);

        invoiceService.createInvoiceForParkingSession(session, calculateFee(session), BigInteger.ZERO, null, null, PaymentStatus.PENDING, LocalDateTime.now());

        if (newStatus == SubStatus.ACTIVE) {
            createParkingSessionBySystem(session, true);
        } else {
            createParkingSessionBySystem(session, false);
        }


    }
    private void createParkingSessionBySystem(ParkingSession session, boolean isMonth) {
        ParkingSession newSession = new ParkingSession();
        newSession.setRootId(session.getRootId());
        newSession.setEntryLane(session.getEntryLane());
        newSession.setVehicleType(session.getVehicleType());
        newSession.setPlateInOcr(session.getPlateInOcr());
        newSession.setFinalPlate(session.getFinalPlate());
        newSession.setConfidenceIn(session.getConfidenceIn());
        newSession.setImageInUrl(session.getImageInUrl());
        newSession.setTimeIn(LocalDateTime.now());
        newSession.setStatus(SessionStatus.PARKED);
        newSession.setMonth(isMonth);
        parkingSessionRepository.save(newSession);
    }
}
