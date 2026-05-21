package smartparkingsystem.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingRuleRequest {

    @NotBlank(message = "Tên quy tắc là bắt buộc")
    @Size(min = 1, max = 100, message = "Tên quy tắc phải từ 1 đến 100 ký tự")
    private String ruleName;

    @NotNull(message = "Loại xe là bắt buộc")
    private VehicleTypeEnum vehicleType;

    @NotNull(message = "Chiến lược tính giá là bắt buộc")
    private PricingStrategyEnum pricingStrategy;

    @DecimalMin(value = "0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
    private BigInteger basePrice;

    @Min(value = 1, message = "Số phút theo block phải ít nhất là 1")
    private Integer blockMinutes;

    @Min(value = 1, message = "Số phút ngưỡng phải ít nhất là 1")
    private Integer thresholdMinutes;

    @DecimalMin(value = "0", message = "Giá ngưỡng phải lớn hơn hoặc bằng 0")
    private BigInteger thresholdPrice;

    @DecimalMin(value = "0", message = "Giá tối đa mỗi ngày phải lớn hơn 0")
    private BigInteger maxPricePerDay;

    private List<TimeWindowAndProgressiveConfig> progressiveConfig;

    @NotNull(message = "Phí phạt là bắt buộc")
    private BigInteger penaltyFee;

    @NotNull(message = "Trạng thái kích hoạt là bắt buộc")
    private Boolean isActive;
}
