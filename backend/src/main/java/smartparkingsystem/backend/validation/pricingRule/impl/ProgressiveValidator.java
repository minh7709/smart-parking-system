package smartparkingsystem.backend.validation.pricingRule.impl;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.validation.pricingRule.PricingStrategyValidator;

import java.math.BigInteger;
import java.util.List;

@Component
public class ProgressiveValidator implements PricingStrategyValidator {
    @Override
    public boolean validate(PricingRuleRequest pricingRuleRequest) {
        boolean check = true;
        if (!this.getPricingStrategyType().equals(pricingRuleRequest.getPricingStrategy())) {
            check = false;
            return check;
        }
        if(pricingRuleRequest.getRuleName() == null ||
                pricingRuleRequest.getProgressiveConfig() == null || pricingRuleRequest.getVehicleType() == null ||
                pricingRuleRequest.getPenaltyFee() == null) {
            check = false;
        }
        List<TimeWindowAndProgressiveConfig> configs = pricingRuleRequest.getProgressiveConfig();

        if (configs.get(0).getFromHour() == null || configs.get(0).getFromHour() != 0) {
            return false;
        }

        for (int i = 0; i < configs.size() - 1; i++) {
            TimeWindowAndProgressiveConfig current = configs.get(i);
            TimeWindowAndProgressiveConfig next = configs.get(i + 1);

            if (current.getToHour() == null || next.getFromHour() == null ||
                    !current.getToHour().equals(next.getFromHour())) {
                return false;
            }
        }
        pricingRuleRequest.setBasePrice(BigInteger.ZERO);
        pricingRuleRequest.setMaxPricePerDay(null);
        pricingRuleRequest.setBlockMinutes(null);
        return check;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.PROGRESSIVE;
    }
}
/*
 private String uleName;
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