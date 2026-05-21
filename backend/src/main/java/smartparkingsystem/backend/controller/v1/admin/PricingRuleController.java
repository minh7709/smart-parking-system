package smartparkingsystem.backend.controller.v1.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.PricingRuleResponse;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.service.admin.PricingRuleService;
import smartparkingsystem.backend.service.auth.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pricing-rules")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;
    private final UserService userService;

    /**
     * Create new pricing rule
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PricingRuleResponse>> createPricingRule(
            @Valid @RequestBody PricingRuleRequest request) {
        log.info("Creating pricing rule: {}", request.getRuleName());
        PricingRuleResponse response = pricingRuleService.createPricingRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Tạo cấu hình giá lượt thành công"));
    }

    /**
     * Update pricing rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> updatePricingRule(
            @PathVariable UUID id,
            @Valid @RequestBody PricingRuleRequest request) {
        log.info("Updating pricing rule with id: {}", id);
        PricingRuleResponse response = pricingRuleService.updatePricingRule(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật cấu hình giá lượt thành công"));
    }

    /**
     * Get pricing rule by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> getPricingRuleById(@PathVariable UUID id) {
        log.info("Fetching pricing rule with id: {}", id);
        PricingRuleResponse response = pricingRuleService.getPricingRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy cấu hình giá lượt thành công"));
    }

    /**
     * List pricing rules with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PricingRuleResponse>>> getAllPricingRules(
            @RequestParam(required = false) VehicleTypeEnum vehicleType,
            Pageable pageable) {
        log.info("Fetching pricing rules with pagination");
        Page<PricingRuleResponse> page = pricingRuleService.getAllPricingRules(pageable, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách cấu hình giá lượt thành công"));
    }


    /**
     * Delete pricing rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePricingRule(@PathVariable UUID id) {
        log.info("Deleting pricing rule with id: {}", id);
        pricingRuleService.deletePricingRule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa cấu hình giá lượt thành công"));
    }

    /**
     * Activate pricing rule
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> activatePricingRule(@PathVariable UUID id) {
        PricingRuleResponse response = pricingRuleService.activatePricingRule(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Kích hoạt cấu hình giá lượt thành công"));
    }

    /**
     * Deactivate pricing rule
     */
    @PostMapping("/{id}/deactivate")
    public void deactivatePricingRule(@PathVariable UUID id) {
        pricingRuleService.deactivatePricingRule(id);
    }
}

