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
    @NotBlank(message = "license plate is required")
    private String licensePlate;
    @NotNull(message = "Subscription duration is required")
    private SubType subType;
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    @NotNull(message = "payment method is required")
    private PaymentMethod paymentMethod;
}
