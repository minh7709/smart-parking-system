package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckOutRequest {
    @NotNull(message = "Exit lane ID is required")
    private UUID exitLaneId;

    @NotNull(message = "parking session is required")
    private UUID parkingSessionId;
}

