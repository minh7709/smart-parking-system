package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.*;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.LaneRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.PricingRuleRepository;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.service.calculator.FeeCalculationFactory;
import smartparkingsystem.backend.service.calculator.FeeCalculationStrategy;
import smartparkingsystem.backend.service.thirdService.AiIntegrationService;

import java.math.BigInteger;
import java.time.LocalDateTime;

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

    public CheckInResponse processCheckIn(CheckInRequest request, MultipartFile image) {
        String licensePlate = aiIntegrationService.getLicensePlateFromAi(image);

        parkingSessionRepository.findByFinalPlate(licensePlate).ifPresent(session -> {
            if (session.getStatus() == SessionStatus.PARKED) {
                throw new DuplicateResourceException("Đã tồn tại phiên đỗ xe với biển số này trong bãi : " + licensePlate);
            }
        });
        Lane lane = laneRepository.findById(request.getEntryLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getEntryLaneId()));
        String imageInUrl;
        ParkingSession newSession = ParkingSession.builder()
                .plateInOcr(licensePlate)
                .entryLane(lane)
                .status(SessionStatus.PARKED)
                .confidenceIn((float)(0.9 + Math.random() * 0.1))
                .build();

        parkingSessionRepository.save(newSession);

        return toCheckInResponse(newSession);
    }

    public CheckInResponse processConfirmCheckIn(ConfirmCheckInRequest request) {
        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe với ID: " + request.getParkingSessionId()));
        if(!session.getPlateInOcr().equals(request.getFinalPlate())){
            incidentService.reportIncident(session, "Biển số xác nhận không khớp với biển số OCR", IncidentTypeEnum.WRONG_PLATE);
        }
        session.setFinalPlate(request.getFinalPlate());
        session.setMonth(false);
        parkingSessionRepository.save(session);

        return toCheckInResponse(session);
    }

    public CheckOutResponse processCheckOut(CheckOutRequest request, MultipartFile image) {
        String licensePlate = aiIntegrationService.getLicensePlateFromAi(image);

        // Tìm session PARKED với biển số này
        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .filter(s -> s.getStatus() == SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số: " + licensePlate));

        BigInteger fee = BigInteger.ZERO;
        if(session.getFinalPlate().equals(licensePlate)) {
            fee.add(calculatePenalty(session));
            fee.add(calculateFee(session));
        } else {
            throw new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số: " + licensePlate);
        }

        invoiceService.createInvoiceForParkingSession(session, fee, userService.getCurrentUser());

        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));
        String imageOutUrl;
        session.setTimeOut(LocalDateTime.now());
        session.setExitLane(lane);
        session.setPlateOutOcr(licensePlate);
        session.setConfidenceOut( (float) (0.9 + Math.random() * 0.1));
        parkingSessionRepository.save(session);

        return CheckOutResponse.builder()
                .id(session.getId())
                .plateOutOcr(licensePlate)
                .finalPlate(session.getFinalPlate())
                .timeOut(session.getTimeOut())
                .status(SessionStatus.PARKED)
                .fee(fee)
                .build();
    }

    public boolean processConfirmCheckOut(ConfirmCheckOutRequest request) {
        ParkingSession session = parkingSessionRepository.findById(request.getParkingSessionId())
                .filter(s -> s.getStatus() == SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với ID: " + request.getParkingSessionId()));
        session.setStatus(SessionStatus.COMPLETED);

        Invoice invoice = invoiceRepository.findByParkingSession(session)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn cho phiên đỗ xe với ID: " + request.getParkingSessionId()));

        invoiceService.updateInvoiceStatus(invoice, PaymentStatus.SUCCESS, request.getPaymentMethod());
        return true;
    }

    private BigInteger calculatePenalty(ParkingSession session){
        PricingRule pricingRule = pricingRuleRepository.findByVehicleTypeAndActiveTrue(session.getVehicleType())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc giá cho loại xe: " + session.getVehicleType()));
        return pricingRule.getPenaltyFee();
    }

    private BigInteger calculateFee(ParkingSession session){
        if(session.isMonth()){
            return BigInteger.ZERO;
        }
        PricingRule pricingRule = pricingRuleRepository.findByVehicleTypeAndActiveTrue(session.getVehicleType())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc giá cho loại xe: " + session.getVehicleType()));
        FeeCalculationStrategy strategy = feeCalculationFactory.getCalculator(pricingRule.getStrategy());
        return strategy.calculateFee(session.getTimeIn(), session.getTimeOut(), pricingRule);
    }

    public CheckOutResponse reportLostCard ( CheckOutWithoutCardRequest request, MultipartFile image){
        String licensePlate = aiIntegrationService.getLicensePlateFromAi(image);
        ParkingSession session = parkingSessionRepository.findByFinalPlate(licensePlate)
                .filter(s -> s.getStatus() == SessionStatus.PARKED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe mở với biển số xe: " + licensePlate));

        incidentService.reportIncident(session, request.getDescription(), IncidentTypeEnum.LOST_CARD);

        Lane lane = laneRepository.findById(request.getExitLaneId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy làn với ID: " + request.getExitLaneId()));
        session.setExitLane(lane);
        session.setTimeOut(LocalDateTime.now());
        session.setPlateOutOcr(licensePlate);
        session.setConfidenceOut( (float) (0.9 + Math.random() * 0.1));
        parkingSessionRepository.save(session);

        BigInteger fee = calculateFee(session).add(calculatePenalty(session));
        invoiceService.createInvoiceForParkingSession(session, fee, userService.getCurrentUser());

        return CheckOutResponse.builder()
                .id(session.getId())
                .plateOutOcr(session.getPlateInOcr())
                .finalPlate(session.getFinalPlate())
                .timeOut(session.getTimeOut())
                .status(SessionStatus.COMPLETED)
                .fee(fee)
                .build();
    }

    public CheckInResponse getParkingSessionByPlate(String plate) {
        ParkingSession session = parkingSessionRepository.findFirstByStatusAndFinalPlateIgnoreCase(
                        SessionStatus.PARKED,
                        plate)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đỗ xe đang mở với biển số: " + plate));

        return toCheckInResponse(session);
    }

    public Page<CheckInResponse> getAllParkingSessions(Pageable pageable, SessionStatus status) {
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<ParkingSession> page = status == null
                ? parkingSessionRepository.findAll(safePageable)
                : parkingSessionRepository.findByStatus(status, safePageable);

        return page.map(this::toCheckInResponse);
    }

    private CheckInResponse toCheckInResponse(ParkingSession session) {
        return CheckInResponse.builder()
                .id(session.getId())
                .plateInOcr(session.getPlateInOcr())
                .finalPlate(session.getFinalPlate())
                .timeIn(session.getTimeIn())
                .status(session.getStatus())
                .isMonth(session.isMonth())
                .vehicleType(session.getVehicleType())
                .build();
    }
}
