package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigInteger;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import smartparkingsystem.backend.dto.response.admin.RevenueBreakdownResponse;
import smartparkingsystem.backend.dto.response.admin.RevenueTimelineResponse;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByParkingSession(ParkingSession parkingSession);
    Optional<Invoice> findBySubscriptionId(UUID subscriptionId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.paymentTime BETWEEN :startDate AND :endDate AND i.status = 'SUCCESS'")
    BigInteger findTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(nativeQuery = true, value = "WITH time_series AS ( " +
            "    SELECT generate_series( " +
            "        date_trunc(:interval, CAST(:startDate AS timestamp)), " +
            "        CAST(:endDate AS timestamp), " +
            "        ('1 ' || :interval)::interval " +
            "    ) AS timestamp " +
            ") " +
            "SELECT " +
            "    ts.timestamp, " +
            "    COALESCE(SUM(i.total_amount), 0) AS totalRevenue " +
            "FROM time_series ts " +
            // ÉP KIỂU status::text
            "LEFT JOIN invoice i ON date_trunc(:interval, i.payment_time) = ts.timestamp " +
            "    AND i.payment_time >= :startDate AND i.payment_time < :endDate AND i.status::text = 'SUCCESS' " +
            "GROUP BY ts.timestamp " +
            "ORDER BY ts.timestamp")
    List<RevenueTimelineResponse> getRevenueTimeline(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("interval") String interval);

    @Query(nativeQuery = true, value = "SELECT " +
            // ÉP KIỂU payment_method::text
            "    COALESCE(SUM(CASE WHEN i.payment_method::text = 'CASH' THEN i.total_amount ELSE 0 END), 0) AS cashRevenue, " +
            "    COALESCE(SUM(CASE WHEN i.payment_method::text = 'ONLINE_PAYMENT' THEN i.total_amount ELSE 0 END), 0) AS onlinePaymentRevenue, " +
            // ÉP KIỂU invoice_type::text
            "    COALESCE(SUM(CASE WHEN i.invoice_type::text = 'PARKING_FEE' THEN i.total_amount ELSE 0 END), 0) AS sessionRevenue, " +
            "    COALESCE(SUM(CASE WHEN i.invoice_type::text = 'SUBSCRIPTION_FEE' THEN i.total_amount ELSE 0 END), 0) AS subscriptionRevenue " +
            "FROM invoice i " +
            "WHERE i.payment_time >= :startDate AND i.payment_time < :endDate AND i.status::text = 'SUCCESS'")
    RevenueBreakdownResponse getRevenueBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(i.penaltyAmount), 0) FROM Invoice i WHERE i.paymentTime BETWEEN :startDate AND :endDate AND i.status = 'SUCCESS'")
    BigInteger getTotalPenalties(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy danh sách hóa đơn trong khoảng thời gian với phân trang
     */
    @Query("SELECT i FROM Invoice i WHERE i.paymentTime >= :startDate AND i.paymentTime < :endDate ORDER BY i.paymentTime DESC")
    Page<Invoice> findByPaymentTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}
