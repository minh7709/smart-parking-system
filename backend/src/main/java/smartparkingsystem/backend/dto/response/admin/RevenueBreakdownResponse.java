package smartparkingsystem.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueBreakdownResponse {
    private BigDecimal cashRevenue;
    private BigDecimal onlinePaymentRevenue;
    private BigDecimal sessionRevenue;
    private BigDecimal subscriptionRevenue;
}
