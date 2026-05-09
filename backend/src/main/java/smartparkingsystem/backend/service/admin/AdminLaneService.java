package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.dto.request.lane.LaneCreateRequest;
import smartparkingsystem.backend.dto.request.lane.LaneUpdateRequest;
import smartparkingsystem.backend.dto.response.LaneResponse;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.mapper.LaneMapper;
import smartparkingsystem.backend.repository.LaneRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminLaneService {
    private final LaneRepository laneRepository;
    private final LaneMapper laneMapper;

    public LaneResponse createLane(LaneCreateRequest request) {
        if (laneRepository.existsByIpCamera(request.getIpCamera())) {
            throw new DuplicateResourceException("Lane with ipCamera '" + request.getIpCamera() + "' already exists");
        }

        Lane lane = Lane.builder()
                .laneName(request.getLaneName())
                .laneType(request.getLaneType())
                .status(request.getStatus())
                .ipCamera(request.getIpCamera())
                .deleted(false)
                .build();

        Lane saved = laneRepository.save(lane);
        log.info("Created lane with id: {}", saved.getId());
        return laneMapper.toLaneResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LaneResponse> getAllLanes() {
        return laneRepository.findAllByDeletedFalse().stream()
                .map(laneMapper::toLaneResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LaneResponse getLaneById(UUID id) {
        Lane lane = laneRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane not found with id: " + id));
        return laneMapper.toLaneResponse(lane);
    }

    public LaneResponse updateLane(UUID id, LaneUpdateRequest request) {
        Lane existing = laneRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane not found with id: " + id));

        if (laneRepository.existsByIpCameraAndIdNot(request.getIpCamera(), id)) {
            throw new DuplicateResourceException("Lane with ipCamera '" + request.getIpCamera() + "' already exists");
        }

        existing.setLaneName(request.getLaneName());
        existing.setLaneType(request.getLaneType());
        existing.setStatus(request.getStatus());
        existing.setIpCamera(request.getIpCamera());

        Lane updated = laneRepository.save(existing);
        log.info("Updated lane with id: {}", id);
        return laneMapper.toLaneResponse(updated);
    }

    public void deleteLane(UUID id) {
        Lane existing = laneRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane not found with id: " + id));

        existing.setDeleted(true);
        laneRepository.save(existing);
        log.info("Soft deleted lane with id: {}", id);
    }
}
