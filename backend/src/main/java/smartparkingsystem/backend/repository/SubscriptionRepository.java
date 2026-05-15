package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.SubType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByVehicleIdAndStatus(UUID vehicleId, SubStatus status);
    Optional<Subscription> findFirstByVehicleIdAndStatusIn(UUID vehicleId, Collection<SubStatus> statuses);
    Optional<Subscription> findByVehicle_LicensePlateIgnoreCaseAndStatus(String licensePlate, SubStatus status);

    List<Subscription> findAllByStatusAndEndDateBefore(SubStatus status, LocalDateTime endDate);

    long countByStatus(SubStatus active);

    boolean existsByVehicle_LicensePlateAndStatus(String plate, SubStatus status);

    Page<Subscription> findAllByStatus(SubStatus subStatus, Pageable pageable);

    Page<Subscription> findAllByStatusAndSubscriptionPricing_DurationType(SubStatus subStatus, SubType subType, Pageable pageable);

    Page<Subscription> findAllBySubscriptionPricing_DurationType(SubType subType, Pageable pageable);

    // Partial licensePlate search methods (LIKE pattern)
    @Query("SELECT s FROM Subscription s WHERE UPPER(s.vehicle.licensePlate) LIKE UPPER(CONCAT('%', :licensePlate, '%'))")
    Page<Subscription> findByVehicleLicensePlateContainingIgnoreCase(@Param("licensePlate") String licensePlate, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE UPPER(s.vehicle.licensePlate) LIKE UPPER(CONCAT('%', :licensePlate, '%')) AND s.status = :status")
    Page<Subscription> findByVehicleLicensePlateContainingIgnoreCaseAndStatus(@Param("licensePlate") String licensePlate, @Param("status") SubStatus status, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE UPPER(s.vehicle.licensePlate) LIKE UPPER(CONCAT('%', :licensePlate, '%')) AND s.subscriptionPricing.durationType = :subType")
    Page<Subscription> findByVehicleLicensePlateContainingIgnoreCaseAndDurationType(@Param("licensePlate") String licensePlate, @Param("subType") SubType subType, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE UPPER(s.vehicle.licensePlate) LIKE UPPER(CONCAT('%', :licensePlate, '%')) AND s.status = :status AND s.subscriptionPricing.durationType = :subType")
    Page<Subscription> findByVehicleLicensePlateContainingIgnoreCaseAndStatusAndDurationType(@Param("licensePlate") String licensePlate, @Param("status") SubStatus status, @Param("subType") SubType subType, Pageable pageable);
}
