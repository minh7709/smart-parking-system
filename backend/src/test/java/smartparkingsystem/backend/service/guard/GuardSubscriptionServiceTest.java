package smartparkingsystem.backend.service.guard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.dto.request.SubscriptionRequest;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.SubscriptionMapper;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.repository.VehicleRepository;
import smartparkingsystem.backend.service.admin.SubscriptionPricingService;
import smartparkingsystem.backend.service.auth.UserService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuardSubscriptionServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private SubscriptionPricingService subscriptionPricingService;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private UserService userService;

    @InjectMocks
    private GuardSubscriptionService guardSubscriptionService;

    @Test
    void createSubscription_startDateInPast_throwException() {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setStartDate(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () -> guardSubscriptionService.createSubscription(request));
    }

    @Test
    void getSubscriptionByLicensePlate_notFound_throwException() {
        when(subscriptionRepository.findByVehicle_LicensePlateIgnoreCaseAndStatus("30A-12345", smartparkingsystem.backend.entity.type.SubStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> guardSubscriptionService.getSubscriptionByLicensePlate("30A-12345"));
    }
}
