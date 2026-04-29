package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.type.SessionStatus;

import java.util.Optional;
import java.util.UUID;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, UUID> {
    Optional<ParkingSession> findByFinalPlate(String finalPlate);

    Optional<ParkingSession> findByFinalPlateAndStatus(String finalPlate, SessionStatus status);


    Optional<ParkingSession> findFirstByIdAndStatus(UUID id, SessionStatus status);

    Page<ParkingSession> findByStatus(SessionStatus status, Pageable pageable);
}
