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
public class FlatRateCalculator implements FeeCalculationStrategy {
    @Override
    public BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule) {
        // Validate input parameters
        if (timeIn == null || timeOut == null) {
            throw new ValidationException(
                "timeIn and timeOut are required to calculate parking fee. " +
                "Both must be provided and in valid LocalDateTime format."
            );
        }

        // Validate time logic - timeOut must be after timeIn
        if (timeOut.isBefore(timeIn)) {
            throw new InvalidStateException(
                String.format(
                    "Invalid parking time range. Check-out time (%s) must be after check-in time (%s). " +
                    "Please verify the time data.",
                    timeOut, timeIn
                )
            );
        }

        // Validate pricing rule exists
        if (rule == null) {
            throw new ResourceNotFoundException(
                "Pricing rule is required to calculate parking fee. " +
                "No active pricing rule found for this vehicle type."
            );
        }
        if(timeIn.toLocalDate().equals(timeOut.toLocalDate())) {
            return rule.getBasePrice();
        }
        LocalDateTime checkInBeginningOfDay = timeIn.toLocalDate().atStartOfDay();
        LocalDateTime checkOutBeginningOfDay = timeOut.toLocalDate().atStartOfDay();
        long days = Duration.between(checkInBeginningOfDay, checkOutBeginningOfDay).toDays();
        return rule.getBasePrice().multiply(BigInteger.valueOf(days + 1));
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.FLAT_RATE;
    }
}
