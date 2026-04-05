package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleTypeEnum {
    CAR ("Xe hơi"), MOTO ("Xe máy");
    private final String label;
}
