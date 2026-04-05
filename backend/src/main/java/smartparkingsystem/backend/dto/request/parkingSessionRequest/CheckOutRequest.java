package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@Validated
public class CheckOutRequest {
    @NotNull(message = "Exit lane ID is required")
    private UUID exitLaneId;

    @NotNull(message = "parking session is required")
    private UUID parkingSessionId;
}

