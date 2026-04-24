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
    boolean existsByIpCamera(String ipCamera);
    boolean existsByIpCameraAndIdNot(String ipCamera, UUID id);
    Optional<Lane> findByIdAndDeletedFalse(UUID id);
    List<Lane> findAllByDeletedFalse();
    List<Lane> findAllByStatusAndDeletedFalse(LaneStatus status);
    boolean existsByIpCameraAndDeletedFalse(String ipCamera);
    boolean existsByIpCameraAndDeletedFalseAndIdNot(String ipCamera, UUID id);
}
