package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@Validated
public class ConfirmCheckInRequest {
    @NotNull(message = "Entry lane ID is required")
    private UUID entryLaneId;

    @NotNull(message = "Plate number is required")
    private String finalPlate;

    @NotNull(message = "parking session id is required")
    private UUID parkingSessionId;
}