package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class CheckOutWithoutCardRequest {
    @NotNull(message = "Entry lane ID is required")
    private UUID exitLaneId;

    @NotNull(message = "description is required")
    private String description;
}
