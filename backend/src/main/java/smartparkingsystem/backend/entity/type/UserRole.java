package smartparkingsystem.backend.entity.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {
    ADMIN ("Quản trị viên"), GUARD ("Bảo vệ");
    private final String label;
    public String getValue() {
        return this.name();
    }
}
