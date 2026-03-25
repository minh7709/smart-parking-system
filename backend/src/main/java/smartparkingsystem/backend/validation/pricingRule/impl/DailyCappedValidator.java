package smartparkingsystem.backend.validation.pricingRule.impl;

import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.validation.pricingRule.PricingStrategyValidator;

public class DailyCappedValidator implements PricingStrategyValidator {
    @Override
    public boolean validate(PricingRuleRequest pricingRule) {

        return true;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.DAILY_CAPPED;
    }
}
