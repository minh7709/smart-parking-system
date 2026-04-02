package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LaneTypeEnum {
    IN ("Lối vào"), OUT ("Lối ra");
    private final String label;
    }
