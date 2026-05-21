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
    @NotBlank(message = "Biển số xe là bắt buộc")
    private String finalPlate;

    @NotBlank(message = "Biển số vào là bắt buộc")
    private String plateInOcr; // Biển số cuối cùng được xác nhận

    @NotBlank(message = "URL ảnh vào là bắt buộc")
    private String imageInUrl; // URL hình ảnh lúc check-in

    @NotNull(message = "Thời gian vào là bắt buộc")
    private LocalDateTime timeIn; // Thời gian check-in

    @NotNull(message = "Độ tin cậy ảnh vào là bắt buộc")
    private Float confidenceIn;

    @NotNull(message = "Loại xe là bắt buộc")
    private VehicleTypeEnum vehicleType; //

    @NotNull(message = "Mã làn vào là bắt buộc")
    private UUID entryLaneId;
}