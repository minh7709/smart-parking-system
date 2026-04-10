package smartparkingsystem.backend.service.calculator;

import smartparkingsystem.backend.entity.Subscription;

import java.math.BigInteger;

public interface SubscriptionCalculationStrategy {
    BigInteger calculateFee(Subscription subscription);
}
