package smartparkingsystem.backend.service.calculator.impl;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.service.calculator.FeeCalculationStrategy;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProgressiveCalculator implements FeeCalculationStrategy {
    @Override
    public BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule) {
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

        long totalMinutes = Math.max(1, Duration.between(timeIn, timeOut).toMinutes());
        long totalHoursRoundedUp = (totalMinutes + 59) / 60; // ceil to the next hour

        List<TimeWindowAndProgressiveConfig> configs = rule.getProgressiveConfig().stream()
                .sorted(Comparator.comparing(TimeWindowAndProgressiveConfig::getFromHour))
                .toList();

        BigInteger fee = BigInteger.ZERO;
        for (TimeWindowAndProgressiveConfig cfg : configs) {
            if (cfg.getFromHour() == null || cfg.getToHour() == null || cfg.getPricePerHour() == null) {
                continue; // skip malformed config entries
            }
            long from = Math.max(0, cfg.getFromHour());
            long to = Math.max(from, cfg.getToHour());
            if (from >= totalHoursRoundedUp) {
                break; // remaining hours are outside session
            }
            long overlapHours = Math.min(totalHoursRoundedUp, to) - from;
            if (overlapHours <= 0) {
                continue;
            }
            BigInteger pricePerHour = BigInteger.valueOf(cfg.getPricePerHour());
            if (Boolean.TRUE.equals(cfg.getIsFixed())) {
                fee = fee.add(pricePerHour);
            } else {
                fee = fee.add(pricePerHour.multiply(BigInteger.valueOf(overlapHours)));
            }
        }

        return fee;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.PROGRESSIVE;
    }
}
