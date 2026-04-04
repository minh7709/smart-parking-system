package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.entity.Incident;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;
import smartparkingsystem.backend.repository.IncidentRepository;
import smartparkingsystem.backend.service.auth.UserService;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final UserService userService;
    public void reportIncident(ParkingSession session, String description, IncidentTypeEnum type) {
        incidentRepository.save(Incident.builder()
                .parkingSession(session)
                .description(description)
                .incidentType(type)
                .reporter(userService.getCurrentUser())
                .build());;
    }
}
