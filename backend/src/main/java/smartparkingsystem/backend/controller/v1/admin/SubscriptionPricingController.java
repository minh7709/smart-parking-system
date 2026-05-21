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
import smartparkingsystem.backend.dto.request.SubscriptionPricingRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.SubscriptionPricingResponse;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.service.admin.SubscriptionPricingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pricing-subscription")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SubscriptionPricingController {

    private final SubscriptionPricingService subscriptionPricingService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPricingResponse>> createSubscriptionPricing(
            @Valid @RequestBody SubscriptionPricingRequest request) {
        log.info("Creating subscription pricing: {}", request.getPricingName());
        SubscriptionPricingResponse response = subscriptionPricingService.createSubscriptionPricing(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo cấu hình giá vé đăng ký thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPricingResponse>> updateSubscriptionPricing(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionPricingRequest request) {
        log.info("Updating subscription pricing with id: {}", id);
        SubscriptionPricingResponse response = subscriptionPricingService.updateSubscriptionPricing(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật cấu hình giá vé đăng ký thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPricing(@PathVariable UUID id) {
        log.info("Deleting subscription pricing with id: {}", id);
        subscriptionPricingService.deleteSubscriptionPricing(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa cấu hình giá vé đăng ký thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPricingResponse>> getSubscriptionPricingById(@PathVariable UUID id) {
        log.info("Fetching subscription pricing with id: {}", id);
        SubscriptionPricingResponse response = subscriptionPricingService.getSubscriptionPricingById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết cấu hình giá vé đăng ký thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SubscriptionPricingResponse>>> getSubscriptionPricings(
            @RequestParam(required = false) VehicleTypeEnum vehicleType,
            Pageable pageable) {
        log.info("Fetching subscription pricing list with pagination");
        Page<SubscriptionPricingResponse> page = subscriptionPricingService.getSubscriptionPricings(pageable, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(page, "Lấy danh sách cấu hình giá vé đăng ký thành công"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Boolean>> activateSubscriptionPricing(@PathVariable UUID id) {
        log.info("Activating subscription pricing with id: {}", id);
        Boolean result = subscriptionPricingService.activateSubscriptionPricing(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Kích hoạt cấu hình giá vé đăng ký thành công"));
    }
}
