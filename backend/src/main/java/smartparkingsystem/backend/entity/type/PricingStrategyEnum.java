package smartparkingsystem.backend.entity.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PricingStrategyEnum {
    FLAT_RATE ("Giá cứng"), TIME_WINDOW ("Khung thời gian"), ROLLING_BLOCK ("Cộng dồn"), PROGRESSIVE ("Cộng lũy tiến"), DAILY_CAPPED ("Cộng dồn + giá trần");
    private final String label;
    public String getValue() {
        return this.name();
    }
}
