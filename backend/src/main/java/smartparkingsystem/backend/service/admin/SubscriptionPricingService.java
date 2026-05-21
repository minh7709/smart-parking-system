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
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
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

    @Transactional
    public SubscriptionPricingResponse createSubscriptionPricing(SubscriptionPricingRequest request) {
        if(subscriptionPricingRepository.existsByPricingName(request.getPricingName())){
            throw new ResourceNotFoundException("Cấu hình giá vé đăng ký với tên '" + request.getPricingName() + "' đã tồn tại");
        }
        if(request.getActive()){
            this.handleSubscriptionPricingActivation(request.getVehicleType(), request.getDurationType());
        }
        SubscriptionPricing subscriptionPricing = subscriptionPricingMapper.toEntity(request, userService.getCurrentUser());
        SubscriptionPricing saved = subscriptionPricingRepository.save(subscriptionPricing);
        return subscriptionPricingMapper.toResponse(saved);
    }
    @Transactional
    public SubscriptionPricingResponse updateSubscriptionPricing(UUID id, SubscriptionPricingRequest request) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Cấu hình giá vé đăng ký"));

        if(!request.getVehicleType().equals(existing.getVehicleType())){
            throw new ValidationException("Loại phương tiện '" + existing.getVehicleType() + "' không thể được thay đổi '");
        }
        if(!request.getDurationType().equals(existing.getDurationType())){
            throw new ValidationException("Loại vé '" + existing.getDurationType() + "' không thể được thay đổi '");
        }

        if(!existing.getPricingName().equals(request.getPricingName()) && subscriptionPricingRepository.existsByPricingName(request.getPricingName())){
            throw new DuplicateResourceException("Cấu hình giá vé đăng ký với tên '" + request.getPricingName() + "' đã tồn tại");
        }
        if(request.getActive() != null && request.getActive() != existing.getActive() && request.getActive()){
            this.handleSubscriptionPricingActivation(request.getVehicleType(), request.getDurationType());
        }
        subscriptionPricingMapper.updateEntity(existing, request);
        SubscriptionPricing updated = subscriptionPricingRepository.save(existing);
        return subscriptionPricingMapper.toResponse(updated);
    }
    @Transactional
    public void deleteSubscriptionPricing(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không thể tìm thấy Cấu hình giá vé đăng ký"));
        if(existing.getActive()){
            throw new InvalidStateException("Không thể xóa Cấu hình giá vé đăng ký đang được kích hoạt. Vui lòng kích hoạt một Cấu hình giá vé đăng ký khác với cùng loại phương tiện và loại vé trước khi xóa Cấu hình giá vé đăng ký này.");
        }
        subscriptionPricingRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public SubscriptionPricingResponse getSubscriptionPricingById(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Cấu hình giá vé đăng ký"));
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
    @Transactional
    public Boolean activateSubscriptionPricing(UUID id) {
        SubscriptionPricing existing = subscriptionPricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Cấu hình giá vé đăng ký"));
        if(existing.getActive()){
            throw new InvalidStateException("Cấu hình giá vé đăng ký đã được kích hoạt");
        }
        handleSubscriptionPricingActivation(existing.getVehicleType(), existing.getDurationType());
        existing.setActive(true);
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
    @Transactional
    public SubscriptionPricing getSubscriptionPricingByDurationTypeAndVehicleType(SubType subType, VehicleTypeEnum vehicleTypeEnum) {
        return subscriptionPricingRepository.findByVehicleTypeAndDurationTypeAndActiveTrue(vehicleTypeEnum, subType)
                .orElseThrow(() -> new ResourceNotFoundException("Không có cấu hình giá vé đăng ký nào được kích hoạt với loại phương tiện: " + vehicleTypeEnum.getLabel() + " và loại vé: " + subType.getLabel()));
    }
}
