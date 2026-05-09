package smartparkingsystem.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrafficTimelineResponse {
    private LocalDateTime timestamp;
    private long regularCount;
    private long monthlyCount;
}
