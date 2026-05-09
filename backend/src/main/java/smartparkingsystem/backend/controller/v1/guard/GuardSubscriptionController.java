package smartparkingsystem.backend.controller.v1.guard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.SubscriptionRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.SubscriptionResponse;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.service.guard.GuardSubscriptionService;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guard/subscriptions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('GUARD')")
public class GuardSubscriptionController {
    private final GuardSubscriptionService subscriptionService;
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getSubscriptions(
            @RequestParam (required = false) SubStatus subStatus,
            @RequestParam (required = false) SubType subType,
            Pageable pageable
            ) {
        Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(pageable, subStatus, subType);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.updateSubscription(request, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(@PathVariable UUID id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription retrieved successfully"));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionByVehicleId(@PathVariable UUID vehicleId) {
        SubscriptionResponse response = subscriptionService.getSubscriptionByVehicleId(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription retrieved successfully"));
    }
}
