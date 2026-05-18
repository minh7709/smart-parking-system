package smartparkingsystem.backend.dto.request;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import jakarta.validation.constraints.*;

@Data
@Validated
public class VehicleRequest {
    @NotBlank(message = "Biển số xe không được để trống")
    @Size(max = 20, message = "Biển số xe không được vượt quá 20 ký tự")
    private String licensePlate;

    @NotNull(message = "Loại xe không được để trống")
    private VehicleTypeEnum vehicleType;

    @NotBlank(message = "Hãng xe không được để trống")
    private String brand;

    @NotBlank(message = "Tên chủ xe không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 11, message = "Số điện thoại phải có 10-11 chữ số")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại chỉ được chứa chữ số")
    private String customerPhone;
}
