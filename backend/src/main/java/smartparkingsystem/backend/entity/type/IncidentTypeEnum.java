package smartparkingsystem.backend.entity.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum IncidentTypeEnum {
    LOST_CARD ("Mất thẻ"), DAMAGE ("Va chạm"), SYSTEM_ERROR ("Lỗi hệ thống"), WRONG_PLATE("Chụp sai biển số"), OTHER ("Khác");
    private final String label;
    public String getValue() {
        return this.name();
    }
}
