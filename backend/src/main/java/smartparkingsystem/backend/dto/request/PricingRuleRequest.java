package smartparkingsystem.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(min = 1, max = 100, message = "Rule name must be between 1 and 100 characters")
    private String ruleName;

    @NotNull(message = "Vehicle type is required")
    private VehicleTypeEnum vehicleType;

    @NotNull(message = "Pricing strategy is required")
    private PricingStrategyEnum pricingStrategy;

    @DecimalMin(value = "0", inclusive = false, message = "Base price must be greater than 0")
    private BigInteger basePrice;

    @Min(value = 1, message = "Block minutes must be at least 1")
    private Integer blockMinutes;

    @Min(value = 1, message = "Threshold minutes must be at least 1")
    private Integer thresholdMinutes;

    @DecimalMin(value = "0", message = "Threshold price must be greater than or equal 0")
    private BigInteger thresholdPrice;

    @DecimalMin(value = "0", message = "Max price per day must be greater than 0")
    private BigInteger maxPricePerDay;

    private List<TimeWindowAndProgressiveConfig> progressiveConfig;

    @NotNull(message = "Penalty price is required")
    private BigInteger penaltyFee;

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
