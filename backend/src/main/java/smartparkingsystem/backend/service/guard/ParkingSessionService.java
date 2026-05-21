package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.dto.request.IncidentRequest;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.*;
import smartparkingsystem.backend.dto.response.ai.AiDetectionResult;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.dto.response.parkingSession.ParkingSessionResponse;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.*;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.ParkingSessionMapper;
import smartparkingsystem.backend.repository.*;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.service.calculator.FeeCalculationFactory;
import smartparkingsystem.backend.service.calculator.FeeCalculationStrategy;
import smartparkingsystem.backend.service.thirdService.AiIntegrationService;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingSessionService {
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
    @Value("${file.upload-dir}")
    private String uploadRootPath;

    private CheckInResponse processCheckInForBicycle(CheckInRequest request, Lane lane, String imageUrl) {
        return parkingSessionMapper.toCheckInResponse("BICYCLE", imageUrl, 1.0f, request.getVehicleType());
    }

    public CheckInResponse processCheckIn(CheckInRequest request, MultipartFile image) {
        Lane lane = laneRepository.findById(request.getEntryLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getEntryLaneId()));


        String imageUrl = storeImage(image, "check-in", "Không thể lưu ảnh check-in");
        if(request.getVehicleType() == VehicleTypeEnum.BICYCLE){
            return processCheckInForBicycle(request, lane, imageUrl);
        }
        AiDetectionResult aiResult = aiIntegrationService.getDetectionResultFromAi(buildAbsoluteImagePath(imageUrl));
        String licensePlate = aiResult.getPlateNumber();
        return parkingSessionMapper.toCheckInResponse(licensePlate, imageUrl, confidenceOrRandom(aiResult.getConfidence()), request.getVehicleType());
    }

    public void cancelCheckIn(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ValidationException("URL ảnh không được để trống");
        }
        imageUrl = imageUrl.trim();
        if (imageUrl.startsWith("\"") && imageUrl.endsWith("\"")) {
            imageUrl = imageUrl.substring(1, imageUrl.length() - 1);
        }

        String relativeImagePath = imageUrl.replace("\\", "/");
        Path baseDir = Path.of(uploadRootPath, "images").toAbsolutePath().normalize();
        Path imagePath = baseDir.resolve(relativeImagePath).normalize();
        if (!imagePath.startsWith(baseDir)) {
            throw new ValidationException("Đường dẫn ảnh không hợp lệ");
        }
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Không thể xóa file ảnh do lỗi hệ thống");
        }
    }

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
        if (!request.getPlateInOcr().equals(request.getFinalPlate())) {
            guardIncidentService.reportIncident(session, "Biển số xác nhận không khớp với biển số OCR", IncidentTypeEnum.WRONG_PLATE);
        }

        parkingSessionRepository.save(session);

        return parkingSessionMapper.toParkingSessionResponse(session);
    }

    private CheckOutResponse processCheckOutForBicycle(ParkingSession session, Lane lane, String imageUrl) {
        parkingSessionMapper.updateEntityForCheckOut(session, lane, imageUrl, 1.0f, "BICYCLE");
        parkingSessionRepository.save(session);
        BigInteger fee = calculateFee(session);
        return parkingSessionMapper.toCheckOutResponse(session, fee, BigInteger.ZERO);
    }

    public CheckOutResponse processCheckOut(CheckOutRequest request, MultipartFile image) {
        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));
        ParkingSession session = parkingSessionRepository.findFirstByIdAndStatus(request.getParkingSessionId(), SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với id: " + request.getParkingSessionId()));
        String imageUrl = storeImage(image, "check-out", "Không thể lưu ảnh check-out");
        if(session.getVehicleType() == VehicleTypeEnum.BICYCLE) {
            return processCheckOutForBicycle(session, lane, imageUrl);
        }

        AiDetectionResult aiResult = aiIntegrationService.getDetectionResultFromAi(buildAbsoluteImagePath(imageUrl));
        String licensePlate = aiResult.getPlateNumber();


        BigInteger fee = BigInteger.ZERO;
        if (session.getFinalPlate().equals(licensePlate)) {
            fee = fee.add(calculateFee(session));
        } else {
            throw new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số: " + licensePlate);
        }
        parkingSessionMapper.updateEntityForCheckOut(session, lane, imageUrl, confidenceOrRandom(aiResult.getConfidence()), licensePlate);
        parkingSessionRepository.save(session);

        return parkingSessionMapper.toCheckOutResponse(session, fee, BigInteger.ZERO);
    }

    public void processConfirmCheckOut(ConfirmCheckOutRequest request) {
        ParkingSession session = parkingSessionRepository.findFirstByIdAndStatus(request.getParkingSessionId(), SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với ID: " + request.getParkingSessionId()));
        session.setStatus(SessionStatus.COMPLETED);
        Invoice invoice = invoiceService.createInvoiceForParkingSession(session, request.getParkingAmount(), userService.getCurrentUser());
        invoiceRepository.save(invoice);
        parkingSessionRepository.save(session);
    }

    public void reportGeneralIncident(IncidentRequest request, MultipartFile evidenceImage) {
        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe với ID: " + request.getParkingSessionId()));

        String evidenceUrl = storeImage(evidenceImage, "evidence", "Không thể lưu ảnh bằng chứng");
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

    public CheckOutResponse reportLostCard(CheckOutWithoutCardRequest request, MultipartFile image, MultipartFile evidenceImage) {
        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));

        String imageUrl = storeImage(image, "check-out", "Không thể lưu ảnh check-out");

        AiDetectionResult aiResult = aiIntegrationService.getDetectionResultFromAi(buildAbsoluteImagePath(imageUrl));
        String licensePlate = aiResult.getPlateNumber();

        ParkingSession session = parkingSessionRepository.findFirstByStatusAndFinalPlateIgnoreCase(SessionStatus.PARKED, licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số xe: " + licensePlate));

        String evidenceUrl = storeImage(evidenceImage, "evidence", "Không thể lưu ảnh bằng chứng");
        guardIncidentService.reportIncident(session, request.getDescription(), IncidentTypeEnum.LOST_CARD, evidenceUrl);
        parkingSessionMapper.updateEntityForCheckOut(session, lane, imageUrl, confidenceOrRandom(aiResult.getConfidence()), licensePlate);
        parkingSessionRepository.save(session);

        BigInteger penalty = calculatePenalty(session);
        BigInteger fee = calculateFee(session);
        

        Invoice invoice = invoiceService.createInvoiceForPenalty(session, penalty, fee, userService.getCurrentUser());

        return parkingSessionMapper.toCheckOutResponse(session, fee, penalty);
    }

    private String storeImage(MultipartFile image, String folder, String failureMessage) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        try {
            // Tạo đường dẫn vật lý tuyệt đối: uploads/images/check-in/
            Path uploadDir = Path.of(uploadRootPath, "images", folder);
            Files.createDirectories(uploadDir);

            String extension = "";
            String originalFileName = image.getOriginalFilename();
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadDir.resolve(fileName);

            // Lưu file
            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = folder + "/" + fileName;
            return relativePath.replace("\\", "/");

        } catch (IOException ex) {
            throw new IllegalStateException(failureMessage, ex);
        }
    }

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
        return getImagePath(imageUrl);
    }

    public Path getImagePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh cho phiên đỗ xe");
        }
        imageUrl = imageUrl.trim();
        if (imageUrl.startsWith("\"") && imageUrl.endsWith("\"")) {
            imageUrl = imageUrl.substring(1, imageUrl.length() - 1);
        }
        Path imagePath = Path.of(uploadRootPath, "images", imageUrl.replace("\\", "/")).toAbsolutePath().normalize();
        if (!Files.exists(imagePath)) {
            throw new ResourceNotFoundException("Không tìm thấy file ảnh: " + imageUrl);
        }

        return imagePath;
    }

    private float confidenceOrRandom(Float confidenceFromAi) {
        if (confidenceFromAi != null) {
            return confidenceFromAi;
        }
        return (float) (0.9 + Math.random() * 0.1);
    }

    private String buildAbsoluteImagePath(String relativeImageUrl) {
        if (relativeImageUrl == null || relativeImageUrl.isBlank()) {
            return null;
        }
        return Path.of(uploadRootPath, "images", relativeImageUrl).toString().replace("\\", "/");
    }
}
