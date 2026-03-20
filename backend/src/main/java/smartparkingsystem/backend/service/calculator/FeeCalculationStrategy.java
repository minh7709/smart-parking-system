package smartparkingsystem.backend.service.calculator;

import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;

public interface FeeCalculationStrategy {
    BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule);
    PricingStrategyEnum getPricingStrategyType();

}
