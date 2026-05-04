package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@Validated
public class ConfirmCheckInRequest {
    @NotNull(message = "Plate number is required")
    private String finalPlate;

    @NotNull(message = "parking session id is required")
    private UUID parkingSessionId;
}