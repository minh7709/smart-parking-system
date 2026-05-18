package smartparkingsystem.backend.entity.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PaymentStatus {
    PENDING ("Đang xử lý"), SUCCESS ("Thành công"), FAILED ("Thất bại");
    private final String label;
}
