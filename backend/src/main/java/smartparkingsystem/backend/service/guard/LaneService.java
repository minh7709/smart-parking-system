package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.dto.response.LaneResponse;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.mapper.LaneMapper;
import smartparkingsystem.backend.repository.LaneRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaneService {
    private final LaneRepository laneRepository;
    private final LaneMapper laneMapper;
    public List<LaneResponse> getActiveLanes() {
        return laneRepository.findAllByStatus(LaneStatus.ACTIVE)
                .stream().map(laneMapper::toLaneResponse).toList();
    }
}
