package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionStatus {
    PARKED ("Đang đỗ"), COMPLETED ("Đã rời"), CANCELLED ("Hủy");
    private final String label;
}
