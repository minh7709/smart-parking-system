package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.type.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smartparkingsystem.backend.dto.response.admin.LaneUtilizationResponse;
import smartparkingsystem.backend.dto.response.admin.TrafficTimelineResponse;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, UUID> {

    Optional<ParkingSession> findFirstByStatusAndFinalPlateIgnoreCase(SessionStatus status, String finalPlate);

    Optional<ParkingSession> findFirstByIdAndStatus(UUID id, SessionStatus status);

    Page<ParkingSession> findByStatus(SessionStatus status, Pageable pageable);

    List<ParkingSession> findAllByFinalPlateIgnoreCase(String finalPlate);

    List<ParkingSession> findAllByFinalPlateIgnoreCaseAndStatus(String finalPlate, SessionStatus status);

    long countByTimeInBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(SessionStatus status);

    boolean existsByEntryLaneAndStatus(Lane lane, SessionStatus status);

    boolean existsByExitLaneAndStatus(Lane lane, SessionStatus status);



    @Query(
        nativeQuery = true,
        value = "WITH time_series AS ( " +
                "    SELECT generate_series( " +
                "        date_trunc(:interval, :startDate::timestamp), " +
                "        :endDate::timestamp, " +
                "        ('1 ' || :interval)::interval " +
                "    ) AS timestamp " +
                ") " +
                "SELECT " +
                "    ts.timestamp, " +
                "    COALESCE(SUM(CASE WHEN ps.is_month = FALSE THEN 1 ELSE 0 END), 0) AS regularCount, " +
                "    COALESCE(SUM(CASE WHEN ps.is_month = TRUE THEN 1 ELSE 0 END), 0) AS monthlyCount " +
                "FROM time_series ts " +
                "LEFT JOIN parking_session ps ON date_trunc(:interval, ps.time_in) = ts.timestamp " +
                "    AND ps.time_in >= :startDate AND ps.time_in < :endDate " +
                "    AND (:vehicleType IS NULL OR ps.vehicle_type = :vehicleType) " +
                "GROUP BY ts.timestamp " +
                "ORDER BY ts.timestamp")
    List<TrafficTimelineResponse> getTrafficTimeline(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     @Param("interval") String interval,
                                                     @Param("vehicleType") String vehicleType);

    @Query(
        nativeQuery = true,
        value = "SELECT " +
                "    l.lane_name AS laneName, " +
                "    (SELECT COUNT(ps.id) FROM parking_session ps WHERE ps.entry_lane_id = l.id AND ps.time_in >= :startDate AND ps.time_in < :endDate) AS entryCount, " +
                "    (SELECT COUNT(ps.id) FROM parking_session ps WHERE ps.exit_lane_id = l.id AND ps.time_out >= :startDate AND ps.time_out < :endDate) AS exitCount " +
                "FROM lane l " +
                "ORDER BY l.lane_name")
    List<LaneUtilizationResponse> getLaneUtilization(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
}
