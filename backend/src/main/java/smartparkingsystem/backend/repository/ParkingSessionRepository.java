package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartparkingsystem.backend.entity.ParkingSession;

import java.util.Optional;
import java.util.UUID;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, UUID> {
    Optional<ParkingSession> findByFinalPlate(String finalPlate);
}
