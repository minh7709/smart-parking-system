package smartparkingsystem.backend.service.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FeeCalculationFactory {
    private final Map<PricingStrategyEnum, FeeCalculationStrategy> calculators;

    @Autowired
    public FeeCalculationFactory(List<FeeCalculationStrategy> calculatorList) {
        this.calculators = calculatorList.stream()
                .collect(Collectors.toMap(FeeCalculationStrategy::getPricingStrategyType, calculator -> calculator));
    }

    public FeeCalculationStrategy getCalculator(PricingStrategyEnum strategy) {
        FeeCalculationStrategy calculator = this.calculators.get(strategy);
        if (calculator == null) {
            throw new IllegalArgumentException("No calculator found for strategy: " + strategy);
        }
        return calculator;
    }
}
