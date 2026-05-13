package smartparkingsystem.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentRequest {
    @NotNull(message = "Parking session ID is required")
    private UUID parkingSessionId;

    private String description;

    @NotNull(message = "Incident type is required")
    private IncidentTypeEnum incidentType;
}