package smartparkingsystem.backend.validation.pricingRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PricingValidatorFactory {
    public final Map<PricingStrategyEnum, PricingStrategyValidator> validators;

    @Autowired
    public PricingValidatorFactory(List<PricingStrategyValidator> validatorList) {
         this.validators = validatorList.stream()
                .collect(Collectors.toMap(PricingStrategyValidator::getPricingStrategyType, v -> v));
    }

    public PricingStrategyValidator getValidator(PricingStrategyEnum strategy) {
        PricingStrategyValidator validator = this.validators.get(strategy);
        if (validator == null) {
            throw new IllegalArgumentException("No validator found for strategy: " + strategy);
        }
        return validator;
    }
}
