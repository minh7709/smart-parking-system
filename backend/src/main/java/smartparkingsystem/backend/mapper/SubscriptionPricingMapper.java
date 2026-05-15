package smartparkingsystem.backend.mapper;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.SubscriptionPricingRequest;
import smartparkingsystem.backend.dto.response.SubscriptionPricingResponse;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.entity.User;

@Component
public class SubscriptionPricingMapper {
    public SubscriptionPricingResponse toResponse(SubscriptionPricing entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPricingResponse.builder()
                .id(entity.getId())
                .pricingName(entity.getPricingName())
                .vehicleType(entity.getVehicleType())
                .description(entity.getDescription())
                .durationType(entity.getDurationType())
                .price(entity.getPrice())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreator() != null ? entity.getCreator().getUsername() : null)
                .build();
    }
    public SubscriptionPricing toEntity(SubscriptionPricingRequest request, User user) {
        if (request == null) {
            return null;
        }

        return SubscriptionPricing.builder()
                .pricingName(request.getPricingName())
                .vehicleType(request.getVehicleType())
                .creator(user)
                .description(request.getDescription())
                .durationType(request.getDurationType())
                .price(request.getPrice())
                .active(request.getActive() != null && request.getActive())
                .build();
    }
    public void updateEntity(SubscriptionPricing entity, SubscriptionPricingRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setDescription(request.getDescription());
        entity.setDurationType(request.getDurationType());
        entity.setPrice(request.getPrice());
        entity.setActive(request.getActive() != null && request.getActive());
    }
}
