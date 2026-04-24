package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.dto.request.SubscriptionPricingRequest;
import smartparkingsystem.backend.dto.response.SubscriptionPricingResponse;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.SubscriptionPricingMapper;
import smartparkingsystem.backend.repository.SubscriptionPricingRepository;
import smartparkingsystem.backend.service.auth.UserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionPricingService {
    private final SubscriptionPricingRepository subscriptionPricingRepository;
    private final SubscriptionPricingMapper subscriptionPricingMapper;
    private final UserService userService;

    public SubscriptionPricingResponse createSubscriptionPricing(SubscriptionPricingRequest request) {
        if(subscriptionPricingRepository.existsByPricingName(request.getPricingName())){
            throw new RuntimeException("Subscription pricing with name '" + request.getPricingName() + "' already exists");
        }
        if(request.getActive()){
            this.handleSubscriptionPricingActivation(request.getVehicleType(), request.getDurationType());
        }
        SubscriptionPricing subscriptionPricing = subscriptionPricingMapper.toEntity(request, userService.getCurrentUser());
        SubscriptionPricing saved = subscriptionPricingRepository.save(subscriptionPricing);
        return subscriptionPricingMapper.toResponse(saved);
    }

    public SubscriptionPricingResponse updateSubscriptionPricing(UUID id, SubscriptionPricingRequest request) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription pricing not found with id: " + id));

        if(!request.getVehicleType().equals(existing.getVehicleType())){
            throw new ValidationException("Vehicle type '" + existing.getVehicleType() + "' cannot be changed to '");
        }
        if(!request.getDurationType().equals(existing.getDurationType())){
            throw new ValidationException("Duration type '" + existing.getDurationType() + "' cannot be changed to '");
        }

        if(subscriptionPricingRepository.existsByPricingName(request.getPricingName())){
            throw new RuntimeException("Subscription pricing with name '" + request.getPricingName() + "' already exists");
        }
        if(request.getActive() != null && request.getActive() != existing.getActive() && request.getActive()){
            this.handleSubscriptionPricingActivation(request.getVehicleType(), request.getDurationType());
        }
        subscriptionPricingMapper.updateEntity(existing, request);
        SubscriptionPricing updated = subscriptionPricingRepository.save(existing);
        return subscriptionPricingMapper.toResponse(updated);
    }

    public void deleteSubscriptionPricing(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription pricing not found with id: " + id));
        if(existing.getActive()){
            throw new RuntimeException("Cannot delete active subscription pricing. Please deactivate it first.");
        }
        subscriptionPricingRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public SubscriptionPricingResponse getSubscriptionPricingById(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription pricing not found with id: " + id));
        return subscriptionPricingMapper.toResponse(existing);
    }
    @Transactional(readOnly = true)
    public Page<SubscriptionPricingResponse> getSubscriptionPricings(Pageable pageable, VehicleTypeEnum vehicleType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "active");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<SubscriptionPricing> page;
        if(vehicleType != null){
            page = subscriptionPricingRepository.findByVehicleType(vehicleType, sortedPageable);
        } else {
            page = subscriptionPricingRepository.findAll(sortedPageable);
        }
        return page.map(subscriptionPricingMapper::toResponse);
    }

    public Boolean activateSubscriptionPricing(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription pricing not found with id: " + id));
        if(existing.getActive()){
            throw new RuntimeException("Subscription pricing is already active");
        }
        existing.setActive(true);
        handleSubscriptionPricingActivation(existing.getVehicleType(), existing.getDurationType());
        subscriptionPricingRepository.save(existing);
        return true;
    }
    private void handleSubscriptionPricingActivation(VehicleTypeEnum vehicleType, SubType durationType) {
        // deactivate any other active subscription pricing with the same vehicle type and duration type
        subscriptionPricingRepository.findByVehicleTypeAndDurationTypeAndActiveTrue(vehicleType, durationType)
                .ifPresent(sp -> {
                    sp.setActive(false);
                    subscriptionPricingRepository.save(sp);
                });
    }
    public void handleSubscriptionPricingDeactivation(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription pricing not found with id: " + id));
        if (!existing.getActive()) {
            throw new RuntimeException("Subscription pricing is already inactive");
        }
        throw new InvalidStateException("Cannot deactivate subscription pricing. Please activate another subscription pricing with the same vehicle type and duration type before deactivating this one.");
    }
}
