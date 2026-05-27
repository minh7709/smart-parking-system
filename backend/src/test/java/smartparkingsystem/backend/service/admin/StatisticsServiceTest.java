package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getSummary_invalidDateRange_throwException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        assertThrows(ValidationException.class, () -> statisticsService.getSummary(start, end));
    }

    @Test
    void getTrafficTimeline_invalidInterval_throwException() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        assertThrows(ValidationException.class,
                () -> statisticsService.getTrafficTimeline(start, end, "YEAR", null));
    }
}
