package smartparkingsystem.backend.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class TimeWindowAndProgressiveConfig {
    private Integer fromHour;
    private Integer toHour;
    private Long price;
}