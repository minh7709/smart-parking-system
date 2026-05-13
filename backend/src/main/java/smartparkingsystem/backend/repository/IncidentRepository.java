package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Incident;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findAll();
    Optional<Incident> findById(UUID id);
    Page<Incident> findByIncidentType(IncidentTypeEnum incidentType, Pageable pageable);
}
