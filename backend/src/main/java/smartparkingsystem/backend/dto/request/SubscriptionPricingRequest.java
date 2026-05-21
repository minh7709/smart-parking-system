package smartparkingsystem.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPricingRequest {
    @NotBlank(message = "Tên gói giá là bắt buộc")
    private String pricingName;

    @NotNull(message = "Loại xe là bắt buộc")
    private VehicleTypeEnum vehicleType;

    @NotNull(message = "Loại thời hạn là bắt buộc")
    private SubType durationType;

    @NotNull(message = "Giá là bắt buộc")
    private BigInteger price;

    private String description;

    @NotNull(message = "Trạng thái kích hoạt là bắt buộc")
    private Boolean active;
}
