package smartparkingsystem.backend.service.guard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.ParkingSessionMapper;
import smartparkingsystem.backend.repository.*;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.service.calculator.FeeCalculationFactory;
import smartparkingsystem.backend.service.thirdService.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSessionServiceTest {
    @Mock
    private AiIntegrationService aiIntegrationService;

    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private FeeCalculationFactory feeCalculationFactory;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private UserService userService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private GuardIncidentService guardIncidentService;

    @Mock
    private ParkingSessionMapper parkingSessionMapper;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ParkingSessionService parkingSessionService;

    @Test
    void cancelCheckIn_shouldDeleteImage() {
        parkingSessionService.cancelCheckIn("check-in/img.jpg");
        verify(fileService).deleteImage("check-in/img.jpg");
    }

    @Test
    void getParkingSessionImagePath_invalidType_throwException() {
        UUID id = UUID.randomUUID();
        ParkingSession session = new ParkingSession();
        session.setImageInUrl("in.jpg");
        session.setImageOutUrl("out.jpg");

        when(parkingSessionRepository.findById(id)).thenReturn(Optional.of(session));

        assertThrows(ValidationException.class, () -> parkingSessionService.getParkingSessionImagePath(id, ""));
    }
}
