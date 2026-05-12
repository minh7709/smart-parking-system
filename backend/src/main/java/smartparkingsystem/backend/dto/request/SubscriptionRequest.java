package smartparkingsystem.backend.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.PaymentMethod;
import smartparkingsystem.backend.entity.type.SubType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionRequest {
    private String licensePlate;
    private SubType subType;
    private LocalDateTime startDate;
    private PaymentMethod paymentMethod;
}
