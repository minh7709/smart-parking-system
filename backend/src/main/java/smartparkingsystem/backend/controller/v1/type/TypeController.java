package smartparkingsystem.backend.controller.v1.type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;
import smartparkingsystem.backend.entity.type.PaymentMethod;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/type")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN','GUARD')")
public class TypeController {

    @GetMapping("/lane-statuses")
    public ResponseEntity<ApiResponse<List<LaneStatus>>> getLaneStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(LaneStatus.values()), "Lane statuses retrieved successfully"));
    }

    @GetMapping("/lane-types")
    public ResponseEntity<ApiResponse<List<LaneTypeEnum>>> getLaneTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(LaneTypeEnum.values()), "Lane types retrieved successfully"));
    }

    @GetMapping("/vehicle-types")
    public ResponseEntity<ApiResponse<List<VehicleTypeEnum>>> getVehicleTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(VehicleTypeEnum.values()), "Vehicle types retrieved successfully"));
    }

    @GetMapping("/session-statuses")
    public ResponseEntity<ApiResponse<List<SessionStatus>>> getSessionStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(SessionStatus.values()), "Session statuses retrieved successfully"));
    }

    @GetMapping("/payment-statuses")
    public ResponseEntity<ApiResponse<List<PaymentStatus>>> getPaymentStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(PaymentStatus.values()), "Payment statuses retrieved successfully"));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPaymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(PaymentMethod.values()), "Payment methods retrieved successfully"));
    }

    @GetMapping("/pricing-strategies")
    public ResponseEntity<ApiResponse<List<PricingStrategyEnum>>> getPricingStrategies() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(PricingStrategyEnum.values()), "Pricing strategies retrieved successfully"));
    }

    @GetMapping("/incident-types")
    public ResponseEntity<ApiResponse<List<IncidentTypeEnum>>> getIncidentTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(IncidentTypeEnum.values()), "Incident types retrieved successfully"));
    }

    @GetMapping("/user-roles")
    public ResponseEntity<ApiResponse<List<UserRole>>> getUserRoles() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(UserRole.values()), "User roles retrieved successfully"));
    }

    @GetMapping("/user-statuses")
    public ResponseEntity<ApiResponse<List<UserStatus>>> getUserStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(UserStatus.values()), "User statuses retrieved successfully"));
    }

    @GetMapping("/subscription-types")
    public ResponseEntity<ApiResponse<List<SubType>>> getSubscriptionTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(SubType.values()), "Subscription types retrieved successfully"));
    }

    @GetMapping("/subscription-statuses")
    public ResponseEntity<ApiResponse<List<SubStatus>>> getSubscriptionStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(SubStatus.values()), "Subscription statuses retrieved successfully"));
    }
}
