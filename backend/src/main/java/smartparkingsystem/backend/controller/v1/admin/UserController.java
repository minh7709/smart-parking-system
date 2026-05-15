package smartparkingsystem.backend.controller.v1.admin;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.user.UserCreateRequest;
import smartparkingsystem.backend.dto.request.user.UserUpdateRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.UserResponse;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.service.admin.AdminUserService;

import java.util.List;
import java.util.UUID;

/**
 * Admin controller - Only ADMIN users can access these endpoints
 * This is protected by @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) UserStatus status
    ) {
        List<UserResponse> response = adminUserService.getAllUsers(status, phone);
        return ResponseEntity.ok(ApiResponse.success(response, "Users fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

}

