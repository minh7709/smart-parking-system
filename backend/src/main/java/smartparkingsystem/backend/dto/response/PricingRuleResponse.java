package smartparkingsystem.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingRuleResponse {

    private UUID id;
    private String ruleName;
    private VehicleTypeEnum vehicleType;
    private PricingStrategyEnum pricingStrategy;
    private BigInteger basePrice;
    private Integer blockMinutes;
    private Integer thresholdMinutes;
    private BigInteger thresholdPrice;
    private BigInteger maxPricePerDay;
    private List<TimeWindowAndProgressiveConfig> progressiveConfig;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
