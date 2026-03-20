package smartparkingsystem.backend.validation.pricingRule;

import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;

public interface PricingStrategyValidator {
    boolean validate(PricingRuleRequest pricingRuleRequest);
    PricingStrategyEnum getPricingStrategyType();
}
