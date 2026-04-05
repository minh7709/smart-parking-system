package smartparkingsystem.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaneResponse {
    private UUID id;
    private String laneName;
    private LaneTypeEnum laneType;
    private String status;
    private String ipCamera;
}