package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.PaymentMethod;

import java.math.BigInteger;
import java.util.UUID;

@Data
@Validated
public class ConfirmCheckOutRequest {
    @NotNull(message = "Phương thức thanh toán là bắt buộc")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Mã phiên gửi xe là bắt buộc")
    private UUID parkingSessionId;

    private BigInteger parkingAmount;
    private BigInteger penaltyAmount;
}
