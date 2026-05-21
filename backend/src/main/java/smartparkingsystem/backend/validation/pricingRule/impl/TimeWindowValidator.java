package smartparkingsystem.backend.validation.pricingRule.impl;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.validation.pricingRule.PricingStrategyValidator;

import java.math.BigInteger;
import java.util.List;

@Component
public class TimeWindowValidator implements PricingStrategyValidator {
    @Override
    public boolean validate(smartparkingsystem.backend.dto.request.PricingRuleRequest pricingRuleRequest) {
        boolean check = true;
        if (!this.getPricingStrategyType().equals(pricingRuleRequest.getPricingStrategy())) {
            check = false;
            return check;
        }
        if(pricingRuleRequest.getRuleName() == null ||
                pricingRuleRequest.getVehicleType() == null ||
                pricingRuleRequest.getProgressiveConfig() == null ||
                pricingRuleRequest.getProgressiveConfig().size() > 2 ||
                pricingRuleRequest.getPenaltyFee() == null
        ) {
            check = false;
        }
        List<TimeWindowAndProgressiveConfig> configs = pricingRuleRequest.getProgressiveConfig();
        int totalHours = 0;

        for (TimeWindowAndProgressiveConfig config : configs) {
            Integer from = config.getFromHour();
            Integer to = config.getToHour();

            if (from == null || to == null || from < 0 || from > 24 || to < 0 || to > 24) {
                return false;
            }

            if (to < from) {
                totalHours += (24 - from) + to;
            } else if (to > from) {
                totalHours += (to - from);
            } else {
                return false; // Giờ bắt đầu và kết thúc trùng nhau không hợp lệ
            }
        }
        if (totalHours != 24) {
            return false;
        }
        pricingRuleRequest.setMaxPricePerDay(null);
        pricingRuleRequest.setBlockMinutes(null);
        pricingRuleRequest.setBasePrice(BigInteger.ZERO);
        return check;
    }

    @Override
    public PricingStrategyEnum getPricingStrategyType() {
        return PricingStrategyEnum.TIME_WINDOW;
    }
}
