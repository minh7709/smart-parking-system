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
public class FlatRateCalculator implements FeeCalculationStrategy {
    @Override
    public BigInteger calculateFee(LocalDateTime timeIn, LocalDateTime timeOut, PricingRule rule) {
        // Validate input parameters
        if (timeIn == null || timeOut == null) {
            throw new ValidationException(
                "Cần có timeIn và timeOut để tính phí gửi xe. " +
                "Cả hai phải được cung cấp và đúng định dạng LocalDateTime."
            );
        }

        // Validate time logic - timeOut must be after timeIn
        if (timeOut.isBefore(timeIn)) {
            throw new InvalidStateException(
                String.format(
                    "Khoảng thời gian gửi xe không hợp lệ. Thời gian ra (%s) phải sau thời gian vào (%s). " +
                    "Vui lòng kiểm tra lại dữ liệu thời gian.",
                    timeOut, timeIn
                )
            );
        }

        // Validate pricing rule exists
        if (rule == null) {
            throw new ResourceNotFoundException(
                "Cần có quy tắc giá để tính phí gửi xe. " +
                "Không tìm thấy quy tắc giá đang hoạt động cho loại xe này."
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
