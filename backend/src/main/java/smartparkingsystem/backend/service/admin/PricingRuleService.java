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
import smartparkingsystem.backend.repository.PricingRuleRepository;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.validation.pricingRule.PricingValidatorFactory;

import java.util.List;
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


    @Transactional
    public PricingRuleResponse createPricingRule(PricingRuleRequest request) {
        // Validate request based on pricing strategy
        validatePricingRuleRequest(request);

        if (pricingRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new DuplicateResourceException("Cấu hình giá vé lượt có tên '" + request.getRuleName() + "' đã tồn tại");
        }

        User creator = userService.getCurrentUser();

        PricingRule pricingRule = pricingRuleMapper.toEntity(request, creator);
        if(pricingRule.isActive()){
            // Nếu rule mới tạo đã được đánh dấu là active, thì cần xử lý tự động tắt rule cũ (nếu có)
            handleRuleActivation(pricingRule);
        }
        PricingRule savedRule = pricingRuleRepository.save(pricingRule);
        return pricingRuleMapper.toResponse(savedRule);
    }

    @Transactional
    public PricingRuleResponse updatePricingRule(UUID id, PricingRuleRequest request) {
        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình giá vé lượt không tìm thấy"));

        if (request.getVehicleType() != pricingRule.getVehicleType()) {
            throw new ValidationException("Không được phép thay đổi loại xe của một cấu hình giá đã tồn tại. Vui lòng tạo cấu hình mới.");
        }

        // Validate request based on pricing strategy
        validatePricingRuleRequest(request);

        // Check if new rule name is unique (if changed)
        if (!pricingRule.getRuleName().equals(request.getRuleName()) &&
                pricingRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new DuplicateResourceException("Cấu hình giá vé lượt có tên '" + request.getRuleName() + "' đã tồn tại");
        }

        // Update fields
        pricingRuleMapper.updateEntity(request, pricingRule);

        PricingRule updatedRule = pricingRuleRepository.save(pricingRule);

        return pricingRuleMapper.toResponse(updatedRule);
    }

    /**
     * Get pricing rule by id
     */
    @Transactional(readOnly = true)
    public PricingRuleResponse getPricingRuleById(UUID id) {
        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình giá vé lượt"));
        return pricingRuleMapper.toResponse(pricingRule);
    }

    /**
     * Get all pricing rules with pagination
     */
    @Transactional(readOnly = true)
    public Page<PricingRuleResponse> getAllPricingRules(Pageable pageable, VehicleTypeEnum vehicleType) {
        // Luôn sort active trước, sau đó mới tới sort client
        Sort sort = Sort.by(Sort.Direction.DESC, "active");
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        Page<PricingRule> page;
        if (vehicleType == null) {
            page = pricingRuleRepository.findAll(sortedPageable);
        } else {
            page = pricingRuleRepository.findByVehicleType(vehicleType, sortedPageable);
        }

        return page.map(pricingRuleMapper::toResponse);
    }

    @Transactional
    public void deletePricingRule(UUID id) {
        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình giá vé lượt"));
        if(pricingRule.isActive()){
            throw new InvalidStateException("Không thể xóa cấu hình giá vé lượt đang hoạt động. Vui lòng hủy kích hoạt nó trước khi xóa.");
        }
        pricingRuleRepository.delete(pricingRule);
        log.info("Pricing rule deleted successfully with id: {}", id);
    }

    /**
     * Activate pricing rule
     */
    @Transactional
    public PricingRuleResponse activatePricingRule(UUID id) {
        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));
        if(pricingRule.isActive()){
            throw new InvalidStateException("Cấu hình giá vé lượt đã đang hoạt động. Vui lòng chọn cấu hình khác để kích hoạt.");
        }
        handleRuleActivation(pricingRule);
        PricingRule updatedRule = pricingRuleRepository.save(pricingRule);

        return pricingRuleMapper.toResponse(updatedRule);
    }

    /**
     * Deactivate pricing rule
     */
    @Transactional
    public void deactivatePricingRule(UUID id) {
        PricingRule pricingRule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + id));
        throw new InvalidStateException(
                "Không thể tắt thủ công cấu hình giá đang hoạt động. " +
                        "Để vô hiệu hóa cấu hình này, vui lòng chọn KÍCH HOẠT một cấu hình giá khác dành cho xe " +
                        pricingRule.getVehicleType() + " để thay thế."
        );
    }

    /// ///////////// validator ///////////////////

    /**
     * Validate pricing rule request based on pricing strategy
     * Uses PricingValidatorFactory to get appropriate validator for strategy
     */
    private void validatePricingRuleRequest(PricingRuleRequest request) {
        if (request.getPricingStrategy() == null) {
            throw new ValidationException("Chiến lược giá không được để trống. Vui lòng chọn một chiến lược giá hợp lệ.");
        }

        // Get validator based on pricing strategy
        var validator = pricingValidatorFactory.getValidator(request.getPricingStrategy());

        // Validate request
        if (!validator.validate(request)) {
            throw new ValidationException(
                    "Chiến lược giá không hợp lệ: " + request.getPricingStrategy() +
                    ". Vui lòng kiểm tra lại dữ liệu gửi lên."
            );
        }

        log.debug("Pricing rule request validated successfully for strategy: {}", request.getPricingStrategy());
    }

    private void handleRuleActivation(PricingRule newActiveRule) {
        // Tìm Cấu hình giá ĐANG HOẠT ĐỘNG của cùng loại xe này
        pricingRuleRepository.findByVehicleTypeAndActiveTrue(newActiveRule.getVehicleType())
                .ifPresent(currentActiveRule -> {
                    // Nếu tìm thấy, tắt nó đi và chốt thời gian kết thúc
                    currentActiveRule.setActive(false);
                    pricingRuleRepository.save(currentActiveRule);
                    log.info("Auto-deactivated previous rule id: {} for vehicle type: {}",
                            currentActiveRule.getId(), currentActiveRule.getVehicleType());
                });

        // Bật Rule mới lên và ghi nhận thời gian bắt đầu
        newActiveRule.setActive(true);
    }
}
