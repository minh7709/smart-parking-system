package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN ("Quản trị viên"), GUARD ("Bảo vệ");
    private final String label;
}
