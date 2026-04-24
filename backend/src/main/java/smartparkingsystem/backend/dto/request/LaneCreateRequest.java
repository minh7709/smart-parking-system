package smartparkingsystem.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;

@Data
public class LaneCreateRequest {
    @NotBlank(message = "Lane name must not be blank")
    @Size(max = 50, message = "Lane name must be at most 50 characters")
    private String laneName;

    @NotNull(message = "Lane type must not be null")
    private LaneTypeEnum laneType;

    @NotNull(message = "Status must not be null")
    private LaneStatus status;

    @NotBlank(message = "IP camera must not be blank")
    @Size(max = 100, message = "IP camera must be at most 100 characters")
    private String ipCamera;
}

