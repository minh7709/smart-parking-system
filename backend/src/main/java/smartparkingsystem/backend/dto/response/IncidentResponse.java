package smartparkingsystem.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    private UUID id;
    private UUID sessionId;
    private String reporterName;
    private String description;
    private LocalDateTime reportedAt;
    private IncidentTypeEnum incidentType;
    private String evidenceUrl;
}

