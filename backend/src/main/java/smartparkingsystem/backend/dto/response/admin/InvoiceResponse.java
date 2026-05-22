package smartparkingsystem.backend.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.InvoiceTypeEnum;
import smartparkingsystem.backend.entity.type.PaymentMethod;
import smartparkingsystem.backend.entity.type.PaymentStatus;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse {
    private UUID id;
    private InvoiceTypeEnum invoiceType;
    private UUID parkingSessionId;
    private UUID subscriptionId;
    private BigInteger parkingAmount;
    private BigInteger penaltyAmount;
    private BigInteger subscriptionAmount;
    private BigInteger totalAmount;
    private String cashierName;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentTime;
}

