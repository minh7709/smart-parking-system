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
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(LaneStatus.values()), "Lấy trạng thái làn thành công"));
    }

    @GetMapping("/lane-types")
    public ResponseEntity<ApiResponse<List<LaneTypeEnum>>> getLaneTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(LaneTypeEnum.values()), "Lấy loại làn thành công"));
    }

    @GetMapping("/vehicle-types")
    public ResponseEntity<ApiResponse<List<VehicleTypeEnum>>> getVehicleTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(VehicleTypeEnum.values()), "Lấy loại xe thành công"));
    }

    @GetMapping("/session-statuses")
    public ResponseEntity<ApiResponse<List<SessionStatus>>> getSessionStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(SessionStatus.values()), "Lấy trạng thái phiên gửi xe thành công"));
    }

    @GetMapping("/payment-statuses")
    public ResponseEntity<ApiResponse<List<PaymentStatus>>> getPaymentStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(PaymentStatus.values()), "Lấy trạng thái thanh toán thành công"));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPaymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(PaymentMethod.values()), "Lấy phương thức thanh toán thành công"));
    }

    @GetMapping("/pricing-strategies")
    public ResponseEntity<ApiResponse<List<PricingStrategyEnum>>> getPricingStrategies() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(PricingStrategyEnum.values()), "Lấy chiến lược tính giá thành công"));
    }

    @GetMapping("/incident-types")
    public ResponseEntity<ApiResponse<List<IncidentTypeEnum>>> getIncidentTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(IncidentTypeEnum.values()), "Lấy loại sự cố thành công"));
    }

    @GetMapping("/user-roles")
    public ResponseEntity<ApiResponse<List<UserRole>>> getUserRoles() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(UserRole.values()), "Lấy vai trò người dùng thành công"));
    }

    @GetMapping("/user-statuses")
    public ResponseEntity<ApiResponse<List<UserStatus>>> getUserStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(UserStatus.values()), "Lấy trạng thái người dùng thành công"));
    }

    @GetMapping("/subscription-types")
    public ResponseEntity<ApiResponse<List<SubType>>> getSubscriptionTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(SubType.values()), "Lấy loại đăng ký thành công"));
    }

    @GetMapping("/subscription-statuses")
    public ResponseEntity<ApiResponse<List<SubStatus>>> getSubscriptionStatuses() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(SubStatus.values()), "Lấy trạng thái đăng ký thành công"));
    }
}
