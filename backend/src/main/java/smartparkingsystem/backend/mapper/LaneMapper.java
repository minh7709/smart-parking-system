package smartparkingsystem.backend.mapper;
import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.LaneRequest;
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
                .status(lane.getStatus() != null ? lane.getStatus().name() : null)
                .ipCamera(lane.getIpCamera())
                .build();
    }
    public Lane toLaneEntity(LaneRequest request) {
        if (request == null) {
            return null;
        }
        Lane lane = new Lane();
        lane.setId(request.getId());
        lane.setLaneName(request.getLaneName());
        lane.setLaneType(request.getLaneType());
        lane.setStatus(request.getStatus());
        lane.setIpCamera(request.getIpCamera());
        return lane;
    }
}
