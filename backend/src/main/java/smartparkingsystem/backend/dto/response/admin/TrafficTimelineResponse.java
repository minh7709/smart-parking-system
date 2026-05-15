package smartparkingsystem.backend.dto.response.admin;

import java.time.LocalDateTime;

public interface TrafficTimelineResponse {
    LocalDateTime getTimestamp();
    long getRegularCount();
    long getMonthlyCount();
}
