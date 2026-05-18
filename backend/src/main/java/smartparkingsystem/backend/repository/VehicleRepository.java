package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Vehicle;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    boolean existsByLicensePlateAndDeletedFalse(String licensePlate);
    Optional<Vehicle> findByIdAndDeletedFalse(UUID id);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByLicensePlateAndDeletedFalse(String licensePlate);
    Page<Vehicle> findByLicensePlateContainingIgnoreCaseAndDeletedFalse(String licensePlate, Pageable pageable);
    Page<Vehicle> findByLicensePlateContainingIgnoreCaseAndVehicleTypeAndDeletedFalse(
            String licensePlate,
            VehicleTypeEnum vehicleType,
            Pageable pageable
    );
    Page<Vehicle> findByVehicleTypeAndDeletedFalse(VehicleTypeEnum vehicleType, Pageable pageable);
    Page<Vehicle> findAllByDeletedFalse(Pageable pageable);
}
