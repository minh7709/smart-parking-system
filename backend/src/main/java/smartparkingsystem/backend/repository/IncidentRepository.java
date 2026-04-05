package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartparkingsystem.backend.entity.Incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findAll();
    Optional<Incident> findById(UUID id);
}
