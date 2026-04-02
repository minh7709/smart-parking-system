package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IncidentTypeEnum {
    LOST_CARD ("Mất thẻ"), DAMAGE ("Va chạm"), SYSTEM_ERROR ("Lỗi hệ thống"), WRONG_PLATE("Chụp sai biển số"), OTHER ("Khác");
    private final String label;
    }
