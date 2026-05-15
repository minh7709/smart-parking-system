package smartparkingsystem.backend.mapper;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.SubscriptionRequest;
import smartparkingsystem.backend.dto.response.SubscriptionResponse;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.entity.Vehicle;
import smartparkingsystem.backend.entity.type.SubStatus;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Component
public class SubscriptionMapper {
    public SubscriptionResponse toResponse(Subscription entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionResponse.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicle() != null ? entity.getVehicle().getId() : null)
                .subType(entity.getSubscriptionPricing().getDurationType())
                .licensePlate(entity.getVehicle().getLicensePlate())
                .price(entity.getPrice())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .subStatus(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    public Subscription toEntity(SubscriptionRequest request, SubscriptionPricing subscriptionPricing, Vehicle vehicle, LocalDateTime endDate) {
        if (request == null) {
            return null;
        }

        return Subscription.builder()
                .vehicle(vehicle)
                .price(subscriptionPricing.getPrice())
                .subscriptionPricing(subscriptionPricing)
                .status(SubStatus.PENDING)
                .startDate(request.getStartDate())
                .endDate(endDate)
                .build();
    }
    public void updateEntity(Subscription entity, SubStatus newStatus) {
        if (entity == null) {
            return;
        }
        entity.setStatus(newStatus);
    }
}
