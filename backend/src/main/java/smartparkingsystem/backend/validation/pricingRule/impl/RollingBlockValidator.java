package smartparkingsystem.backend.validation.pricingRule.impl;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.validation.pricingRule.PricingStrategyValidator;

@Component
public class RollingBlockValidator implements PricingStrategyValidator {
    @Override
    public boolean validate(PricingRuleRequest pricingRuleRequest) {
        boolean check = true;
        if (!this.getPricingStrategyType().equals(pricingRuleRequest.getPricingStrategy())) {
            check = false;
            return check;
        }
        if(pricingRuleRequest.getRuleName() == null || pricingRuleRequest.getBasePrice() == null ||
                pricingRuleRequest.getStartTime() == null ||
                pricingRuleRequest.getBlockMinutes() == null || pricingRuleRequest.getVehicleType() == null ||
                pricingRuleRequest.getPenaltyFee() == null) {
            check = false;
        }
        pricingRuleRequest.setProgressiveConfig(null);
        return check;
    }
    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.ROLLING_BLOCK;
    }
}

/*
 private String ruleName;
 private VehicleTypeEnum vehicleType;
 private PricingStrategyEnum pricingStrategy;
 private BigInteger basePrice;
 private LocalDateTime startTime;
 private LocalDateTime endTime;
 private Integer blockMinutes;
 private Integer thresholdMinutes;
 private BigInteger thresholdPrice;
 private BigInteger maxPricePerDay;
 private List<ProgressivePriceConfig> progressiveConfig;
 private Boolean isActive;
 */