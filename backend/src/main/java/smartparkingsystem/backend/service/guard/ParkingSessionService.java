package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.*;
import smartparkingsystem.backend.dto.response.ai.AiDetectionResult;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.mapper.ParkingSessionMapper;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.LaneRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.PricingRuleRepository;
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
import java.util.UUID;

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
    private final IncidentService incidentService;
    private final ParkingSessionMapper parkingSessionMapper;

    @Value("${file.upload-dir}")
    private String uploadRootPath;

    private CheckInResponse processCheckInForBicycle(CheckInRequest request, Lane lane, String imageUrl) {
        ParkingSession newSession = parkingSessionMapper.toEntityForCheckIn(
                request,
                lane,
                "BICYCLE",
                1.0f
        );
        newSession.setImageInUrl(imageUrl);
        parkingSessionRepository.save(newSession);
        return parkingSessionMapper.toCheckInResponse(newSession);
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

        parkingSessionRepository.findByFinalPlateAndStatus(licensePlate, SessionStatus.PARKED).ifPresent(existingSession -> {
                throw new DuplicateResourceException("Đã tồn tại phiên đỗ xe với biển số này trong bãi : " + licensePlate);
        });

        ParkingSession newSession = parkingSessionMapper.toEntityForCheckIn(
                request,
                lane,
                licensePlate,
                confidenceOrRandom(aiResult.getConfidence())
        );
        newSession.setImageInUrl(imageUrl);

        parkingSessionRepository.save(newSession);
        return parkingSessionMapper.toCheckInResponse(newSession);
    }

    public CheckInResponse processConfirmCheckIn(ConfirmCheckInRequest request) {
        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe với ID: " + request.getParkingSessionId()));
        if (!session.getPlateInOcr().equals(request.getFinalPlate())) {
            incidentService.reportIncident(session, "Biển số xác nhận không khớp với biển số OCR", IncidentTypeEnum.WRONG_PLATE);
        }
        session.setFinalPlate(request.getFinalPlate());
        session.setMonth(false);
        parkingSessionRepository.save(session);

        return parkingSessionMapper.toCheckInResponse(session);
    }

    private CheckOutResponse processCheckOutForBicycle(ParkingSession session, Lane lane, String imageUrl) {

        session.setTimeOut(LocalDateTime.now());
        session.setExitLane(lane);
        session.setPlateOutOcr("BICYCLE");
        session.setConfidenceOut(1.0f);
        session.setImageOutUrl(imageUrl);
        parkingSessionRepository.save(session);

        BigInteger fee = calculateFee(session);
        invoiceService.createInvoiceForParkingSession(session, fee, userService.getCurrentUser());

        return parkingSessionMapper.toCheckOutResponse(session, fee);
    }

    public CheckOutResponse processCheckOut(CheckOutRequest request, MultipartFile image) {
        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));

        String imageUrl = storeImage(image, "check-out", "Không thể lưu ảnh check-out");

        ParkingSession session = parkingSessionRepository.findFirstByIdAndStatus(request.getParkingSessionId(), SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với id: " + request.getParkingSessionId()));

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

        invoiceService.createInvoiceForParkingSession(session, fee, userService.getCurrentUser());

        session.setTimeOut(LocalDateTime.now());
        session.setExitLane(lane);
        session.setPlateOutOcr(licensePlate);
        session.setConfidenceOut(confidenceOrRandom(aiResult.getConfidence()));
        session.setImageOutUrl(imageUrl);
        parkingSessionRepository.save(session);

        return parkingSessionMapper.toCheckOutResponse(session, fee);
    }

    public void processConfirmCheckOut(ConfirmCheckOutRequest request) {
        ParkingSession session = parkingSessionRepository.findFirstByIdAndStatus(request.getParkingSessionId(), SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với ID: " + request.getParkingSessionId()));
        session.setStatus(SessionStatus.COMPLETED);

        Invoice invoice = invoiceRepository.findByParkingSession(session)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn cho phiên đỗ xe với ID: " + request.getParkingSessionId()));

        invoiceService.updateInvoiceStatus(invoice, PaymentStatus.SUCCESS, request.getPaymentMethod());
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

        ParkingSession session = parkingSessionRepository.findByFinalPlateAndStatus(licensePlate, SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số xe: " + licensePlate));

        String evidenceUrl = storeImage(evidenceImage, "evidence", "Không thể lưu ảnh bằng chứng");
        incidentService.reportIncident(session, request.getDescription(), IncidentTypeEnum.LOST_CARD, evidenceUrl);


        session.setExitLane(lane);
        session.setTimeOut(LocalDateTime.now());
        session.setPlateOutOcr(licensePlate);
        session.setConfidenceOut(confidenceOrRandom(aiResult.getConfidence()));
        session.setImageOutUrl(imageUrl);
        session.setStatus(SessionStatus.COMPLETED);
        parkingSessionRepository.save(session);

        Invoice invoice = invoiceService.createInvoiceForPenalty(session, calculatePenalty(session), calculateFee(session), userService.getCurrentUser());

        return parkingSessionMapper.toCheckOutResponse(session, invoice.getTotalAmount());
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

            // Trả về URL để Frontend hiển thị (hoặc lưu vào DB)
            // cấu hình Resource Handler trong Spring để map URL này với thư mục vật lý
            return folder + "/" + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException(failureMessage, ex);
        }
    }

    public CheckInResponse getParkingSessionByPlate(String plate) {
        ParkingSession session = parkingSessionRepository.findFirstByStatusAndFinalPlateIgnoreCase(
                        SessionStatus.PARKED,
                        plate
                )
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe đang mở với biển số: " + plate));

        return parkingSessionMapper.toCheckInResponse(session);
    }

    public Page<CheckInResponse> getAllParkingSessions(Pageable pageable, SessionStatus status) {
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<ParkingSession> page = status == null
                ? parkingSessionRepository.findAll(safePageable)
                : parkingSessionRepository.findByStatus(status, safePageable);

        return page.map(parkingSessionMapper::toCheckInResponse);
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
        return Path.of(uploadRootPath, "images", relativeImageUrl).toString();
    }
}
