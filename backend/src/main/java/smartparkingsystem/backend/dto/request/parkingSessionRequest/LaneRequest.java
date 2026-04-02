package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;

import java.util.UUID;

@Getter
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LaneRequest {
    private UUID id;
    @NotBlank(message = "Lane name must not be blank")
    private String laneName;

    @NotNull(message = "Lane type must not be null")
    private LaneTypeEnum laneType;

    @NotNull(message = "Status must not be null")
    private LaneStatus status;

    @NotBlank(message = "IP camera must not be blank")
    private String ipCamera;
}
