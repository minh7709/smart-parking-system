package smartparkingsystem.backend.dto.response.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RevenueTimelineResponse {
    LocalDateTime getTimestamp();
    BigDecimal getTotalRevenue();
}
