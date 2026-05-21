package smartparkingsystem.backend.service.calculator.impl.pricingParkingStrategy;

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

@Component
public class TimeWindowCalculator implements FeeCalculationStrategy {
    private static final int MINUTES_PER_DAY = 24 * 60;
    @Override
    public BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule) {
        // Validate input times
        if (timeIn == null || timeOut == null) {
            throw new ValidationException(
                "Cần có timeIn và timeOut để tính phí gửi xe. " +
                "Cả hai phải được cung cấp và đúng định dạng LocalDateTime."
            );
        }

        // Validate time logic
        if (timeOut.isBefore(timeIn)) {
            throw new InvalidStateException(
                String.format(
                    "Khoảng thời gian gửi xe không hợp lệ. Thời gian ra (%s) phải sau thời gian vào (%s). " +
                    "Vui lòng kiểm tra lại dữ liệu thời gian.",
                    timeOut, timeIn
                )
            );
        }

        if (timeIn.toLocalDate().equals(timeOut.toLocalDate())) {
            for(TimeWindowAndProgressiveConfig cfg : rule.getProgressiveConfig()) {
                if (cfg.getFromHour() == null || cfg.getToHour() == null || cfg.getPrice() == null) {
                    continue; // skip malformed config entries
                }
                BigInteger price = BigInteger.valueOf(cfg.getPrice());
                if (isTimeInConfigWindow(timeOut, cfg)) {
                    return price;
                }
            }
        }

        BigInteger fee = BigInteger.ZERO;
        if(timeIn.toLocalDate().equals(timeOut.toLocalDate())) {
            for(TimeWindowAndProgressiveConfig cfg : rule.getProgressiveConfig()) {
                if (cfg.getFromHour() == null || cfg.getToHour() == null || cfg.getPrice() == null) {
                    continue; // skip malformed config entries
                }
                BigInteger price = BigInteger.valueOf(cfg.getPrice());
                if (isTimeInConfigWindow(timeOut, cfg)) {
                    fee = fee.add(price);
                }
            }
            return fee;
        }
        BigInteger maxPrice = null;
        for(TimeWindowAndProgressiveConfig cfg : rule.getProgressiveConfig()) {
            if (cfg.getFromHour() == null || cfg.getToHour() == null || cfg.getPrice() == null) {
                continue; // skip malformed config entries
            }
            BigInteger price = BigInteger.valueOf(cfg.getPrice());
            if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                maxPrice = price;
            }
            if (isTimeInConfigWindow(timeOut, cfg)) {
                fee = fee.add(price);
            }
        }
        LocalDateTime beginDayTimeIn = timeIn.toLocalDate().atStartOfDay();
        LocalDateTime beginDayTimeOut = timeOut.toLocalDate().atStartOfDay();
        long minutesBetween = Duration.between(beginDayTimeIn, beginDayTimeOut).toMinutes();
        long days = minutesBetween / MINUTES_PER_DAY;

        fee = maxPrice.multiply(BigInteger.valueOf(days)).add(fee);

        return fee;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.TIME_WINDOW;
    }

    private boolean isTimeInConfigWindow(LocalDateTime time, TimeWindowAndProgressiveConfig config) {
        int hour = time.getHour();
        int start = config.getFromHour();
        int end = config.getToHour();

        if (start < end) {
            return hour >= start && hour < end;
        } else {
            return hour >= start || hour < end;
        }
    }
}