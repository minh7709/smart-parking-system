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
    @NotNull(message = "Mã phiên gửi xe là bắt buộc")
    private UUID parkingSessionId;

    private String description;

    @NotNull(message = "Loại sự cố là bắt buộc")
    private IncidentTypeEnum incidentType;
}