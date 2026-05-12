package smartparkingsystem.backend.repository;

import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;
import tools.jackson.databind.ext.OptionalHandlerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import smartparkingsystem.backend.dto.response.admin.RevenueBreakdownResponse;
import smartparkingsystem.backend.dto.response.admin.RevenueTimelineResponse;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByParkingSessionId(UUID parkingSessionId);
    Optional<Invoice> findByParkingSession(ParkingSession parkingSession);
    Optional<Invoice> findBySubscriptionId(UUID subscriptionId);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentTime BETWEEN :startDate AND :endDate")
    BigDecimal findTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(nativeQuery = true, value = "WITH time_series AS ( " +
            "    SELECT generate_series( " +
            "        date_trunc(:interval, :startDate::timestamp), " +
            "        :endDate::timestamp, " +
            "        ('1 ' || :interval)::interval " +
            "    ) AS timestamp " +
            ") " +
            "SELECT " +
            "    ts.timestamp, " +
            "    COALESCE(SUM(i.total_amount), 0) AS totalRevenue " +
            "FROM time_series ts " +
            "LEFT JOIN invoice i ON date_trunc(:interval, i.payment_time) = ts.timestamp " +
            "    AND i.payment_time >= :startDate AND i.payment_time < :endDate AND i.status = 'SUCCESS' " +
            "GROUP BY ts.timestamp " +
            "ORDER BY ts.timestamp")
    List<RevenueTimelineResponse> getRevenueTimeline(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("interval") String interval);

    @Query(nativeQuery = true, value = "SELECT " +
            "    COALESCE(SUM(CASE WHEN i.payment_method = 'CASH' THEN i.total_amount ELSE 0 END), 0) AS cashRevenue, " +
            "    COALESCE(SUM(CASE WHEN i.payment_method = 'ONLINE_PAYMENT' THEN i.total_amount ELSE 0 END), 0) AS onlinePaymentRevenue, " +
            "    COALESCE(SUM(CASE WHEN i.session_id IS NOT NULL THEN i.total_amount ELSE 0 END), 0) AS sessionRevenue, " +
            "    COALESCE(SUM(CASE WHEN i.sub_id IS NOT NULL THEN i.total_amount ELSE 0 END), 0) AS subscriptionRevenue " +
            "FROM invoice i " +
            "WHERE i.payment_time >= :startDate AND i.payment_time < :endDate AND i.status = 'SUCCESS'")
    RevenueBreakdownResponse getRevenueBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(i.penaltyAmount) FROM Invoice i WHERE i.paymentTime BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPenalties(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
