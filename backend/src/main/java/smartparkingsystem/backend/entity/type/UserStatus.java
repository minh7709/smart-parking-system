package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE ("Hoạt động"), INACTIVE("Không hoạt động");
    private final String label;
}
