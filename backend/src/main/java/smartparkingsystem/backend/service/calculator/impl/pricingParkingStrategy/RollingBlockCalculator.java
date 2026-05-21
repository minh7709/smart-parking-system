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

        // Validate pricing rule and base price
        if (rule == null || rule.getBasePrice() == null) {
            throw new ResourceNotFoundException(
                "Cần có quy tắc giá với basePrice để tính phí gửi xe. " +
                "Không tìm thấy quy tắc giá đang hoạt động cho loại xe này."
            );
        }

        // Validate rolling block configuration
        if (rule.getBlockMinutes() == null || rule.getBlockMinutes() <= 0) {
            throw new ValidationException(
                "blockMinutes phải được cung cấp và lớn hơn 0 cho chiến lược tính theo block. " +
                "Giá trị hiện tại: " + rule.getBlockMinutes()
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
