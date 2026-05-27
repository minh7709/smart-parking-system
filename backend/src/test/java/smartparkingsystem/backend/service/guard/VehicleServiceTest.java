package smartparkingsystem.backend.service.guard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.entity.Vehicle;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.mapper.VehicleMapper;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.repository.VehicleRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {
    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void deleteVehicle_hasActiveSubscription_throwException() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle();

        when(vehicleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(vehicle));
        when(subscriptionRepository.findByVehicleIdAndStatusIn(id, java.util.List.of(
                smartparkingsystem.backend.entity.type.SubStatus.PENDING,
                smartparkingsystem.backend.entity.type.SubStatus.ACTIVE
        ))).thenReturn(Optional.of(new smartparkingsystem.backend.entity.Subscription()));

        assertThrows(InvalidStateException.class, () -> vehicleService.deleteVehicle(id));
    }
}
