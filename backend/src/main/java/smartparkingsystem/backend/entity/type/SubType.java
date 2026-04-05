package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubType {
MONTHLY("Tháng"), QUARTERLY("Quý"), YEARLY("Năm");
    private final String label;
}

