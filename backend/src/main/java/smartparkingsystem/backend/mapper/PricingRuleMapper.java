package smartparkingsystem.backend.mapper;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.dto.response.PricingRuleResponse;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.User;

/**
 * Mapper for PricingRule entity and DTOs
 */
@Component
public class PricingRuleMapper {

    /**
     * Convert PricingRule entity to response DTO
     */
    public PricingRuleResponse toResponse(PricingRule entity) {
        if (entity == null) {
            return null;
        }

        return PricingRuleResponse.builder()
                .id(entity.getId())
                .ruleName(entity.getRuleName())
                .vehicleType(entity.getVehicleType())
                .pricingStrategy(entity.getStrategy())
                .basePrice(entity.getBasePrice())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .blockMinutes(entity.getBlockMinutes())
                .thresholdMinutes(entity.getThresholdMinutes())
                .thresholdPrice(entity.getThresholdPrice())
                .maxPricePerDay(entity.getMaxPricePerDay())
                .progressiveConfig(entity.getProgressiveConfig())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreator() != null ? entity.getCreator().getUsername() : null)
                .build();
    }

    /**
     * Convert request DTO to PricingRule entity
     */
    public PricingRule toEntity(PricingRuleRequest request, User creator) {
        if (request == null) {
            return null;
        }

        return PricingRule.builder()
                .ruleName(request.getRuleName())
                .vehicleType(request.getVehicleType())
                .strategy(request.getPricingStrategy())
                .basePrice(request.getBasePrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .blockMinutes(request.getBlockMinutes())
                .thresholdMinutes(request.getThresholdMinutes())
                .thresholdPrice(request.getThresholdPrice())
                .maxPricePerDay(request.getMaxPricePerDay())
                .progressiveConfig(request.getProgressiveConfig())
                .active(request.getIsActive() != null && request.getIsActive())
                .creator(creator)
                .build();
    }

    /**
     * Update PricingRule entity from request DTO
     */
    public void updateEntity(PricingRuleRequest request, PricingRule entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setRuleName(request.getRuleName());
        entity.setVehicleType(request.getVehicleType());
        entity.setStrategy(request.getPricingStrategy());
        entity.setBasePrice(request.getBasePrice());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setBlockMinutes(request.getBlockMinutes());
        entity.setThresholdMinutes(request.getThresholdMinutes());
        entity.setThresholdPrice(request.getThresholdPrice());
        entity.setMaxPricePerDay(request.getMaxPricePerDay());
        entity.setProgressiveConfig(request.getProgressiveConfig());
    }
}
