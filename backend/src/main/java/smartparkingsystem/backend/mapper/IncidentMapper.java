package smartparkingsystem.backend.mapper;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.IncidentRequest;
import smartparkingsystem.backend.dto.response.IncidentResponse;
import smartparkingsystem.backend.entity.Incident;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.User;

@Component
public class IncidentMapper {
    public IncidentResponse toResponse(Incident incident) {
        if (incident == null) {
            return null;
        }
        return IncidentResponse.builder()
                .id(incident.getId())
                .sessionId(incident.getParkingSession().getId())
                .reporterName(incident.getReporter().getFullName())
                .reporterId(incident.getReporter().getId())
                .description(incident.getDescription())
                .reportedAt(incident.getReportedAt())
                .incidentType(incident.getIncidentType())
                .evidenceUrl(incident.getEvidenceUrl())
                .build();
    }

    public Incident toEntity(IncidentRequest request, ParkingSession parkingSession, User reporter, String evidenceUrl) {
        if(request == null) {
            return null;
        }
        return Incident.builder()
                .description(request.getDescription())
                .incidentType(request.getIncidentType())
                .reporter(reporter)
                .parkingSession(parkingSession)
                .evidenceUrl(evidenceUrl)
                .build();
    }
}
