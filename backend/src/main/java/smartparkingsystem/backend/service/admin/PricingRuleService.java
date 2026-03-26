package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.dto.response.PricingRuleResponse;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.PricingRuleMapper;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.repository.PricingRuleRepository;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.validation.pricingRule.PricingValidatorFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PricingRuleService {
    private final PricingRuleMapper pricingRuleMapper;
    private final PricingRuleRepository pricingRuleRepository;
    private final UserService userService;
    private final PricingValidatorFactory pricingValidatorFactory;

    /**
     * Create new pricing rule
     */
    @Transactional
    public PricingRuleResponse createPricingRule(PricingRuleRequest request) {
        log.info("Creating pricing rule: {}", request.getRuleName());

        // Validate request based on pricing strategy
        validatePricingRuleRequest(request);

        // Check if rule name already exists
        if (pricingRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new DuplicateResourceException("Pricing rule with name '" + request.getRuleName() + "' already exists");
        }

        // Get creator user
        User creator = userService.getCurrentUser();

        PricingRule pricingRule = pricingRuleMapper.toEntity(request, creator);
        if(pricingRule.isActive()){
            // Nếu rule mới tạo đã được đánh dấu là active, thì cần xử lý tự động tắt rule cũ (nếu có)
            handleRuleActivation(pricingRule);
        }
        PricingRule savedRule = pricingRuleRepository.save(pricingRule);
        log.info("Pricing rule created successfully with id: {}", savedRule.getId());

        return pricingRuleMapper.toResponse(savedRule);
    }

    /**
     * Update existing pricing rule
     */
    @Transactional
    public PricingRuleResponse updatePricingRule(UUID id, PricingRuleRequest request, UUID updatedByUserId) {
        log.info("Updating pricing rule with id: {}", id);

        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));

        if (request.getVehicleType() != pricingRule.getVehicleType()) {
            throw new ValidationException("Không được phép thay đổi loại xe của một cấu hình giá đã tồn tại. Vui lòng tạo cấu hình mới.");
        }

        // Validate request based on pricing strategy
        validatePricingRuleRequest(request);

        // Check if new rule name is unique (if changed)
        if (!pricingRule.getRuleName().equals(request.getRuleName()) &&
                pricingRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new DuplicateResourceException("Pricing rule with name '" + request.getRuleName() + "' already exists");
        }

        // Update fields
        pricingRuleMapper.updateEntity(request, pricingRule);

        PricingRule updatedRule = pricingRuleRepository.save(pricingRule);
        log.info("Pricing rule updated successfully with id: {}", id);

        return pricingRuleMapper.toResponse(updatedRule);
    }

    /**
     * Get pricing rule by id
     */
    @Transactional(readOnly = true)
    public PricingRuleResponse getPricingRuleById(UUID id) {
        log.info("Fetching pricing rule with id: {}", id);

        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));

        return pricingRuleMapper.toResponse(pricingRule);
    }

    /**
     * Get all pricing rules with pagination
     */
    @Transactional(readOnly = true)
    public Page<PricingRuleResponse> getAllPricingRules(Pageable pageable, String vehicleType) {
        log.info("Fetching all pricing rules with pagination");
        // Luôn sort active trước, sau đó mới tới sort client
        Sort sort = Sort.by(Sort.Direction.DESC, "active");
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        Page<PricingRule> page;
        if (vehicleType == null || vehicleType.isBlank()) {
            page = pricingRuleRepository.findAll(sortedPageable);
        } else {
            VehicleTypeEnum typeEnum = VehicleTypeEnum.valueOf(vehicleType);
            page = pricingRuleRepository.findByVehicleType(typeEnum, sortedPageable);
        }

        return page.map(pricingRuleMapper::toResponse);
    }

    /**
     * Delete pricing rule
     */
    @Transactional
    public void deletePricingRule(UUID id) {
        log.info("Deleting pricing rule with id: {}", id);

        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));

        pricingRuleRepository.delete(pricingRule);
        log.info("Pricing rule deleted successfully with id: {}", id);
    }

    /**
     * Activate pricing rule
     */
    @Transactional
    public PricingRuleResponse activatePricingRule(UUID id) {
        log.info("Activating pricing rule with id: {}", id);

        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));

        handleRuleActivation(pricingRule);
        PricingRule updatedRule = pricingRuleRepository.save(pricingRule);

        return pricingRuleMapper.toResponse(updatedRule);
    }

    /**
     * Deactivate pricing rule
     */
    @Transactional
    public PricingRuleResponse deactivatePricingRule(UUID id) {
        log.info("Deactivating pricing rule with id: {}", id);

        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));
        validateDeactivation(pricingRule);
        pricingRule.setActive(false);
        PricingRule updatedRule = pricingRuleRepository.save(pricingRule);

        return pricingRuleMapper.toResponse(updatedRule);
    }

    /// ///////////// validator ///////////////////

    /**
     * Validate pricing rule request based on pricing strategy
     * Uses PricingValidatorFactory to get appropriate validator for strategy
     */
    private void validatePricingRuleRequest(PricingRuleRequest request) {
        if (request.getPricingStrategy() == null) {
            throw new ValidationException("Pricing strategy cannot be null");
        }

        // Get validator based on pricing strategy
        var validator = pricingValidatorFactory.getValidator(request.getPricingStrategy());

        // Validate request
        if (!validator.validate(request)) {
            throw new ValidationException(
                    "Invalid pricing rule request for strategy: " + request.getPricingStrategy() +
                    ". Please check all required fields are provided correctly."
            );
        }

        log.debug("Pricing rule request validated successfully for strategy: {}", request.getPricingStrategy());
    }

    /**
     * Logic xử lý khi kích hoạt một Rule: Tự động vô hiệu hóa Rule cũ
     */
    private void handleRuleActivation(PricingRule newActiveRule) {
        // Tìm Cấu hình giá ĐANG HOẠT ĐỘNG của cùng loại xe này
        pricingRuleRepository.findByVehicleTypeAndActiveTrue(newActiveRule.getVehicleType())
                .ifPresent(currentActiveRule -> {
                    // Nếu tìm thấy, tắt nó đi và chốt thời gian kết thúc
                    currentActiveRule.setActive(false);
                    currentActiveRule.setEndTime(LocalDateTime.now());
                    pricingRuleRepository.save(currentActiveRule);
                    log.info("Auto-deactivated previous rule id: {} for vehicle type: {}",
                            currentActiveRule.getId(), currentActiveRule.getVehicleType());
                });

        // Bật Rule mới lên và ghi nhận thời gian bắt đầu
        newActiveRule.setActive(true);
        newActiveRule.setStartTime(LocalDateTime.now());
        newActiveRule.setEndTime(null);
    }

    /**
     * Logic kiểm tra trước khi vô hiệu hóa một Rule
     */
    private void validateDeactivation(PricingRule ruleToDeactivate) {
        if (!ruleToDeactivate.isActive()) {
            return; // Đã tắt rồi thì bỏ qua
        }

        // CHỐT CHẶN BẢO VỆ: Nếu rule này đang active, người dùng KHÔNG ĐƯỢC phép tắt thủ công.
        // Vì nếu tắt, bãi xe sẽ không có rule để tính tiền cho loại xe này.
        // Cách đúng là: Bật một rule khác, hệ thống sẽ TỰ ĐỘNG tắt rule này.
        throw new InvalidStateException(
                "Không thể tắt thủ công cấu hình giá đang hoạt động. " +
                        "Để vô hiệu hóa cấu hình này, vui lòng chọn KÍCH HOẠT một cấu hình giá khác dành cho xe " +
                        ruleToDeactivate.getVehicleType() + " để thay thế."
        );
    }

}
