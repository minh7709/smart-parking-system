package smartparkingsystem.backend.mapper;
import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.lane.LaneCreateRequest;
import smartparkingsystem.backend.dto.response.LaneResponse;
import smartparkingsystem.backend.entity.Lane;
@Component
public class LaneMapper {
    public LaneResponse toLaneResponse(Lane lane) {
        if (lane == null) {
            return null;
        }
        return LaneResponse.builder()
                .id(lane.getId())
                .laneName(lane.getLaneName())
                .laneType(lane.getLaneType())
                .status(lane.getStatus() != null ? lane.getStatus() : null)
                .ipCamera(lane.getIpCamera())
                .build();
    }
    public Lane toLaneEntity(LaneCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Lane.builder()
                .laneName(request.getLaneName())
                .laneType(request.getLaneType())
                .ipCamera(request.getIpCamera())
                .status(request.getStatus())
                .build();
    }
}
