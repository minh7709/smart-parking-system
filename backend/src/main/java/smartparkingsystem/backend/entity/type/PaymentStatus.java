package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING ("Đang xử lý"), SUCCESS ("Thành công"), FAILED ("Thất bại");
    private final String label;
}
