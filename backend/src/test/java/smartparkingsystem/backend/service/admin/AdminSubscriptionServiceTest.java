package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.service.guard.ParkingSessionService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSubscriptionServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ParkingSessionService parkingSessionService;

    @InjectMocks
    private AdminSubscriptionService adminSubscriptionService;

    @Test
    void cancelSubscription_invalidStatus_throwException() {
        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(SubStatus.EXPIRED);

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));

        assertThrows(InvalidStateException.class, () -> adminSubscriptionService.cancelSubscription(id));
    }

    @Test
    void cancelSubscription_invoiceNotFound_throwException() {
        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(SubStatus.PENDING);

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        when(invoiceRepository.findBySubscription(subscription)).thenReturn(Optional.empty());

        assertThrows(smartparkingsystem.backend.exception.ResourceNotFoundException.class,
                () -> adminSubscriptionService.cancelSubscription(id));
    }
}
