package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Validated
public class ConfirmCheckInRequest {
    @NotBlank(message = "Plate number is required")
    private String finalPlate;

    @NotBlank(message = "Plate number in is required")
    private String plateInOcr; // Biển số cuối cùng được xác nhận

    @NotBlank(message = "Image in URL is required")
    private String imageInUrl; // URL hình ảnh lúc check-in

    @NotNull(message = "time in is required")
    private LocalDateTime timeIn; // Thời gian check-in

    @NotNull(message = "confidence in is required")
    private Float confidenceIn;

    @NotNull(message = "vehicle type is required")
    private VehicleTypeEnum vehicleType; //

    @NotBlank(message = "entry lane id is required")
    private UUID entryLaneId;
}