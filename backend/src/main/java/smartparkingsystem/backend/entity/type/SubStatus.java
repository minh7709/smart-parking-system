package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubStatus {
    PENDING ("Đang xử lý"), ACTIVE ("Còn hạn"), EXPIRED ("Hết hạn"), CANCELLED ("Đã hủy");
    private final String label;
}
