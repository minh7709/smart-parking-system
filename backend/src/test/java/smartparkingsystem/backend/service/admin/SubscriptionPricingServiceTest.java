package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.repository.SubscriptionPricingRepository;
import smartparkingsystem.backend.mapper.SubscriptionPricingMapper;
import smartparkingsystem.backend.service.auth.UserService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionPricingServiceTest {
    @Mock
    private SubscriptionPricingRepository subscriptionPricingRepository;

    @Mock
    private SubscriptionPricingMapper subscriptionPricingMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubscriptionPricingService subscriptionPricingService;

    @Test
    void deleteSubscriptionPricing_active_throwException() {
        UUID id = UUID.randomUUID();
        SubscriptionPricing pricing = new SubscriptionPricing();
        pricing.setActive(true);

        when(subscriptionPricingRepository.findById(id)).thenReturn(Optional.of(pricing));

        assertThrows(InvalidStateException.class, () -> subscriptionPricingService.deleteSubscriptionPricing(id));
    }

    @Test
    void activateSubscriptionPricing_alreadyActive_throwException() {
        UUID id = UUID.randomUUID();
        SubscriptionPricing pricing = new SubscriptionPricing();
        pricing.setActive(true);

        when(subscriptionPricingRepository.findById(id)).thenReturn(Optional.of(pricing));

        assertThrows(InvalidStateException.class, () -> subscriptionPricingService.activateSubscriptionPricing(id));
    }
}
