package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Getter
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckOutWithoutCardRequest {
    @NotNull(message = "Entry lane ID is required")
    private UUID exitLaneId;

    private String description;
}
