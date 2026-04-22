package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleTypeEnum {
    CAR ("Xe hơi"), MOTOR ("Xe máy"), BICYCLE ("Xe đạp");
    private final String label;
}
