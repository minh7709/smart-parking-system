package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import smartparkingsystem.backend.entity.Incident;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.mapper.IncidentMapper;
import smartparkingsystem.backend.repository.IncidentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminIncidentServiceTest {
    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentMapper incidentMapper;

    @InjectMocks
    private AdminIncidentService adminIncidentService;

    @Test
    void getEvidence_emptyUrl_throwException() {
        UUID id = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setEvidenceUrl(" ");

        ReflectionTestUtils.setField(adminIncidentService, "uploadRootPath", "uploads");
        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        assertThrows(ResourceNotFoundException.class, () -> adminIncidentService.getEvidence(id));
    }
}
