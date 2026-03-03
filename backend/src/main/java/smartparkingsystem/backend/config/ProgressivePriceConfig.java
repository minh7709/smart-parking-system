package smartparkingsystem.backend.config;

import lombok.Data;

@Data
public class ProgressivePriceConfig {
    private Integer fromHour;
    private Integer toHour;
    private Long pricePerHour;
    private Boolean isFixed; // co phai gia co dinh cho khoang thoi gian nay khong
}