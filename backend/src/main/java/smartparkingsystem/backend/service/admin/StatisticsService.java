package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.dto.response.admin.PenaltyFeesResponse;
import smartparkingsystem.backend.dto.response.admin.RevenueBreakdownResponse;
import smartparkingsystem.backend.dto.response.admin.RevenueTimelineResponse;
import smartparkingsystem.backend.dto.response.admin.LaneUtilizationResponse;
import smartparkingsystem.backend.dto.response.admin.SummaryResponse;
import smartparkingsystem.backend.dto.response.admin.TrafficTimelineResponse;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final InvoiceRepository invoiceRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SummaryResponse getSummary(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        BigInteger totalRevenueBigInt = invoiceRepository.findTotalRevenueBetween(startDate, endDate);
        BigDecimal totalRevenue = new BigDecimal(totalRevenueBigInt);
        long totalSessions = parkingSessionRepository.countByTimeInBetween(startDate, endDate);
        long parkedCount = parkingSessionRepository.countByStatus(SessionStatus.PARKED);
        long activeSubscriptions = subscriptionRepository.countByStatus(SubStatus.ACTIVE);

        return new SummaryResponse(totalRevenue, totalSessions, parkedCount, activeSubscriptions);
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("startDate và endDate không được để trống");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("startDate phải nhỏ hơn hoặc bằng endDate");
        }
    }

    private void validateInterval(String interval) {
        if (interval == null) {
            throw new ValidationException("Interval không được để trống");
        }
        String upperInterval = interval.toUpperCase();
        if (!upperInterval.equals("HOUR") && !upperInterval.equals("DAY") &&
            !upperInterval.equals("WEEK") && !upperInterval.equals("MONTH")) {
            throw new ValidationException("Interval không hợp lệ. Chỉ chấp nhận: HOUR, DAY, WEEK, MONTH");
        }
    }

    public List<TrafficTimelineResponse> getTrafficTimeline(LocalDateTime startDate, LocalDateTime endDate, String interval, VehicleTypeEnum vehicleType) {
        validateDateRange(startDate, endDate);
        validateInterval(interval);
        return parkingSessionRepository.getTrafficTimeline(startDate, endDate, interval.toUpperCase(), vehicleType);
    }

    public List<LaneUtilizationResponse> getLaneUtilization(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        return parkingSessionRepository.getLaneUtilization(startDate, endDate);
    }

    public List<RevenueTimelineResponse> getRevenueTimeline(LocalDateTime startDate, LocalDateTime endDate, String interval) {
        validateDateRange(startDate, endDate);
        validateInterval(interval);
        return invoiceRepository.getRevenueTimeline(startDate, endDate, interval.toUpperCase());
    }

    public RevenueBreakdownResponse getRevenueBreakdown(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        return invoiceRepository.getRevenueBreakdown(startDate, endDate);
    }

    public PenaltyFeesResponse getTotalPenalties(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        BigInteger totalPenaltiesBigInt = invoiceRepository.getTotalPenalties(startDate, endDate);
        BigDecimal totalPenalties = new BigDecimal(totalPenaltiesBigInt);
        return new PenaltyFeesResponse(totalPenalties);
    }
}