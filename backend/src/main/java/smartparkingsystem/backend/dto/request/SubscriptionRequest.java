package smartparkingsystem.backend.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.PaymentMethod;
import smartparkingsystem.backend.entity.type.SubType;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionRequest {
    @NotBlank(message = "Biển số xe là bắt buộc")
    private String licensePlate;
    @NotNull(message = "Thời hạn đăng ký là bắt buộc")
    private SubType subType;
    @NotNull(message = "Ngày bắt đầu là bắt buộc")
    private LocalDateTime startDate;
    @NotNull(message = "Phương thức thanh toán là bắt buộc")
    private PaymentMethod paymentMethod;
}
