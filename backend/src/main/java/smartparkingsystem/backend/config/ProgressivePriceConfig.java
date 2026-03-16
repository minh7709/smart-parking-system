package smartparkingsystem.backend.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ProgressivePriceConfig {
    private Integer fromHour;
    private Integer toHour;
    private Long pricePerHour;
    private Boolean isFixed; // co phai gia co dinh cho khoang thoi gian nay khong
}