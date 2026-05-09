package smartparkingsystem.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Vehicle type is required")
    private VehicleTypeEnum vehicleType;

    @NotBlank(message = "Duration type is required")
    private SubType durationType;

    @NotBlank(message = "Price is required")
    private BigInteger price;

    private String description;

    @NotBlank(message = "Active status is required")
    private Boolean active;
}
