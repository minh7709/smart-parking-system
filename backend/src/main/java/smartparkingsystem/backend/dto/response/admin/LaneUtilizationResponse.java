package smartparkingsystem.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LaneUtilizationResponse {
    private String laneName;
    private long entryCount;
    private long exitCount;
}
