package smartparkingsystem.backend.service.calculator.impl;

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
public class DailyCappedCalculator implements FeeCalculationStrategy {
    private static final int MINUTES_PER_DAY = 24 * 60;

    @Override
    public BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule){
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

        // Validate daily cap configuration
        if (rule.getMaxPricePerDay() == null || rule.getMaxPricePerDay().compareTo(BigInteger.ZERO) <= 0) {
            throw new ValidationException(
                "maxPricePerDay must be provided and greater than 0 for daily capped strategy. " +
                "Current value: " + rule.getMaxPricePerDay()
            );
        }

        int blockMinutes = (rule.getBlockMinutes() != null && rule.getBlockMinutes() > 0)
                ? rule.getBlockMinutes()
                : MINUTES_PER_DAY;

        long totalMinutes = Math.max(1, Duration.between(timeIn, timeOut).toMinutes());
        long blocks = (totalMinutes + blockMinutes - 1) / blockMinutes;

        BigInteger fee = rule.getBasePrice().multiply(BigInteger.valueOf(blocks));

        long days = (totalMinutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY;
        BigInteger dailyCap = rule.getMaxPricePerDay().multiply(BigInteger.valueOf(days));
        if (fee.compareTo(dailyCap) > 0) {
            fee = dailyCap;
        }

        return fee;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.DAILY_CAPPED;
    }
}