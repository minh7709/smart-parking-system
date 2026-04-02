package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.PaymentMethod;

import java.util.UUID;

@Data
@Validated
public class ConfirmCheckOutRequest {
    @NotNull(message = "Plate number is required")
    private String finalPlate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Parking session ID is required")
    private UUID parkingSessionId;
}
