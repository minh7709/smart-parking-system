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
    @NotBlank(message = "Pricing name is required")
    private String pricingName;

    @NotNull(message = "Vehicle type is required")
    private VehicleTypeEnum vehicleType;

    @NotNull(message = "Duration type is required")
    private SubType durationType;

    @NotNull(message = "Price is required")
    private BigInteger price;

    private String description;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
