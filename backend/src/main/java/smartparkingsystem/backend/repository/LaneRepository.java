package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.type.LaneStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LaneRepository extends JpaRepository<Lane, UUID> {
    Optional<Lane> findById(UUID id);
    List<Lane> findAllByStatus(LaneStatus status);
}
