package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Getter
@Validated
public class CheckOutWithoutCardRequest {
    @NotNull(message = "Entry lane ID is required")
    private UUID exitLaneId;

    @NotNull(message = "description is required")
    private String description;
}
