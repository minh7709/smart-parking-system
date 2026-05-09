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
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final InvoiceRepository invoiceRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SummaryResponse getSummary(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalRevenue = invoiceRepository.findTotalRevenueBetween(startDate, endDate);
        long totalSessions = parkingSessionRepository.countByTimeInBetween(startDate, endDate);
        long parkedCount = parkingSessionRepository.countByStatus(SessionStatus.PARKED);
        long activeSubscriptions = subscriptionRepository.countByStatus(SubStatus.ACTIVE);

        return new SummaryResponse(totalRevenue, totalSessions, parkedCount, activeSubscriptions);
    }

    public List<TrafficTimelineResponse> getTrafficTimeline(LocalDateTime startDate, LocalDateTime endDate, String interval, VehicleTypeEnum vehicleType) {
        String vehicleTypeStr = (vehicleType != null) ? vehicleType.name() : null;
        return parkingSessionRepository.getTrafficTimeline(startDate, endDate, interval.toUpperCase(), vehicleTypeStr);
    }

    public List<LaneUtilizationResponse> getLaneUtilization(LocalDateTime startDate, LocalDateTime endDate) {
        return parkingSessionRepository.getLaneUtilization(startDate, endDate);
    }

    public List<RevenueTimelineResponse> getRevenueTimeline(LocalDateTime startDate, LocalDateTime endDate, String interval) {
        return invoiceRepository.getRevenueTimeline(startDate, endDate, interval.toUpperCase());
    }

    public RevenueBreakdownResponse getRevenueBreakdown(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.getRevenueBreakdown(startDate, endDate);
    }

    public PenaltyFeesResponse getTotalPenalties(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalPenalties = invoiceRepository.getTotalPenalties(startDate, endDate);
        return new PenaltyFeesResponse(totalPenalties);
    }
}