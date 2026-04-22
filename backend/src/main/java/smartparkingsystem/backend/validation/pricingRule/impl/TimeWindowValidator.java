package smartparkingsystem.backend.validation.pricingRule.impl;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.validation.pricingRule.PricingStrategyValidator;

@Component
public class TimeWindowValidator implements PricingStrategyValidator {
    @Override
    public boolean validate(smartparkingsystem.backend.dto.request.PricingRuleRequest pricingRuleRequest) {
        boolean check = true;
        if (!this.getPricingStrategyType().equals(pricingRuleRequest.getPricingStrategy())) {
            check = false;
            return check;
        }
        if(pricingRuleRequest.getRuleName() == null || pricingRuleRequest.getBasePrice() == null ||
                pricingRuleRequest.getVehicleType() == null ||
                pricingRuleRequest.getProgressiveConfig() == null ||
                pricingRuleRequest.getProgressiveConfig().size() > 2 ||
                pricingRuleRequest.getPenaltyFee() == null
        ) {
            check = false;
        }
        pricingRuleRequest.setMaxPricePerDay(null);
        pricingRuleRequest.setBlockMinutes(null);
        pricingRuleRequest.setThresholdMinutes(null);
        pricingRuleRequest.setThresholdPrice(null);
        return check;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.TIME_WINDOW;
    }
}
