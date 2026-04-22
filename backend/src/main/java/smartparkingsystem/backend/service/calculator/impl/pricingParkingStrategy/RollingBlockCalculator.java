package smartparkingsystem.backend.service.calculator.impl.pricingParkingStrategy;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.service.calculator.FeeCalculationStrategy;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class RollingBlockCalculator implements FeeCalculationStrategy {
    @Override
    public BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule) {
        // Validate input times
        if (timeIn == null || timeOut == null) {
            throw new ValidationException(
                "timeIn and timeOut are required to calculate parking fee. " +
                "Both must be provided and in valid LocalDateTime format."
            );
        }

        // Validate time logic
        if (timeOut.isBefore(timeIn)) {
            throw new InvalidStateException(
                String.format(
                    "Invalid parking time range. Check-out time (%s) must be after check-in time (%s). " +
                    "Please verify the time data.",
                    timeOut, timeIn
                )
            );
        }

        // Validate pricing rule and base price
        if (rule == null || rule.getBasePrice() == null) {
            throw new ResourceNotFoundException(
                "Pricing rule with basePrice is required to calculate parking fee. " +
                "No active pricing rule found for this vehicle type."
            );
        }

        // Validate rolling block configuration
        if (rule.getBlockMinutes() == null || rule.getBlockMinutes() <= 0) {
            throw new ValidationException(
                "blockMinutes must be provided and greater than 0 for rolling block strategy. " +
                "Current value: " + rule.getBlockMinutes()
            );
        }

        long totalMinutes = Math.max(1, Duration.between(timeIn, timeOut).toMinutes());
        long blocks = (totalMinutes + rule.getBlockMinutes() - 1) / rule.getBlockMinutes();

        return rule.getBasePrice().multiply(BigInteger.valueOf(blocks));
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.ROLLING_BLOCK;
    }
}
