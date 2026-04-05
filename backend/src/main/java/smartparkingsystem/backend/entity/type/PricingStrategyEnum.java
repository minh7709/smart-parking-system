package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PricingStrategyEnum {
    FLAT_RATE ("Giá cứng"), TIME_WINDOW ("Khung thời gian"), ROLLING_BLOCK ("Cộng dồn"), PROGRESSIVE ("Cộng lũy tiến"), DAILY_CAPPED ("Cộng dồn + giá trần");
    private final String label;
}
