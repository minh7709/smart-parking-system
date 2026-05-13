package smartparkingsystem.backend.dto.response.admin;

import java.math.BigDecimal;

public interface RevenueBreakdownResponse {
    BigDecimal getCashRevenue();
    BigDecimal getOnlinePaymentRevenue();
    BigDecimal getSessionRevenue();
    BigDecimal getSubscriptionRevenue();
}
