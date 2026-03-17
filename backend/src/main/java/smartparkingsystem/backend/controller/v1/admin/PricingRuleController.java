package smartparkingsystem.backend.controller.v1.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.PricingRuleResponse;
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
                .body(ApiResponse.success(response, "Pricing rule created successfully"));
    }

    /**
     * Update pricing rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> updatePricingRule(
            @PathVariable UUID id,
            @Valid @RequestBody PricingRuleRequest request) {
        log.info("Updating pricing rule with id: {}", id);
        UUID currentUserId = userService.getCurrentUser().getId();
        PricingRuleResponse response = pricingRuleService.updatePricingRule(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Pricing rule updated successfully"));
    }

    /**
     * Get pricing rule by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> getPricingRuleById(@PathVariable UUID id) {
        log.info("Fetching pricing rule with id: {}", id);
        PricingRuleResponse response = pricingRuleService.getPricingRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Pricing rule fetched successfully"));
    }

    /**
     * List pricing rules with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PricingRuleResponse>>> getAllPricingRules(
            @PageableDefault(sort = "active", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching pricing rules with pagination");
        Page<PricingRuleResponse> page = pricingRuleService.getAllPricingRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Pricing rules fetched successfully"));
    }


    /**
     * Delete pricing rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePricingRule(@PathVariable UUID id) {
        log.info("Deleting pricing rule with id: {}", id);
        pricingRuleService.deletePricingRule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Pricing rule deleted successfully"));
    }

    /**
     * Activate pricing rule
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> activatePricingRule(@PathVariable UUID id) {
        log.info("Activating pricing rule with id: {}", id);
        PricingRuleResponse response = pricingRuleService.activatePricingRule(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Pricing rule activated successfully"));
    }

    /**
     * Deactivate pricing rule
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> deactivatePricingRule(@PathVariable UUID id) {
        log.info("Deactivating pricing rule with id: {}", id);
        PricingRuleResponse response = pricingRuleService.deactivatePricingRule(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Pricing rule deactivated successfully"));
    }
}

