package smartparkingsystem.backend.controller.v1.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.SubscriptionResponse;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.service.admin.AdminSubscriptionService;
import smartparkingsystem.backend.service.guard.GuardSubscriptionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;
    private final GuardSubscriptionService guardSubscriptionService;

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmSubscription(
            @PathVariable UUID id,
            @RequestParam SubStatus newStatus) {
        adminSubscriptionService.confirmSubscription(id, newStatus);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái đăng ký thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getSubscriptions(
            Pageable pageable,
            @RequestParam(required = false) SubStatus subStatus,
            @RequestParam(required = false) SubType subType) {
        Page<SubscriptionResponse> subscriptions = guardSubscriptionService.getSubscriptions(pageable, subStatus, subType);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Lấy danh sách đăng ký thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        SubscriptionResponse response = guardSubscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết đăng ký thành công"));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionByVehicleId(@PathVariable UUID vehicleId) {
        SubscriptionResponse response = guardSubscriptionService.getSubscriptionByVehicleId(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin đăng ký của phương tiện thành công"));
    }
}
