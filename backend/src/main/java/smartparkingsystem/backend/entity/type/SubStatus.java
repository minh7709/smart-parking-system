package smartparkingsystem.backend.entity.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SubStatus {
    PENDING ("Đang xử lý"), ACTIVE ("Còn hạn"), EXPIRED ("Hết hạn"), CANCELLED ("Đã hủy");
    private final String label;
    public String getValue() {
        return this.name();
    }
}
