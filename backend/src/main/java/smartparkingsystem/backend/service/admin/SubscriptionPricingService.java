package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.dto.request.SubscriptionPricingRequest;
import smartparkingsystem.backend.dto.response.SubscriptionPricingResponse;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.mapper.SubscriptionPricingMapper;
import smartparkingsystem.backend.repository.SubscriptionPricingRepository;
import smartparkingsystem.backend.service.auth.UserService;

@Service
@RequiredArgsConstructor
public class SubscriptionPricingService {
    private final SubscriptionPricingRepository subscriptionPricingRepository;
    private final SubscriptionPricingMapper subscriptionPricingMapper;
    private final UserService userService;

    public SubscriptionPricingResponse createSubscriptionPricing(SubscriptionPricingRequest request) {
        SubscriptionPricing subscriptionPricing = subscriptionPricingMapper.toEntity(request, userService.getCurrentUser());
        SubscriptionPricing saved = subscriptionPricingRepository.save(subscriptionPricing);
        return subscriptionPricingMapper.toResponse(saved);
    }

}
