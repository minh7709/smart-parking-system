package smartparkingsystem.backend.controller.v1.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.auth.*;
import smartparkingsystem.backend.dto.request.PhoneRequest;
import smartparkingsystem.backend.dto.response.LoginResponse;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.service.auth.AuthService;

import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.service.auth.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @SecurityRequirements
    @Operation(summary = "Admin login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }

    @SecurityRequirements
    @Operation(summary = "Refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginResponse response = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(response,"Refresh Token thành công"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GUARD')")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest logoutRequest,
                                                    @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        String accessToken = extractBearerToken(authorizationHeader);
        authService.logout(accessToken, logoutRequest.getRefreshToken(), username);

        log.info("User logged out successfully: {}", username);
        return ResponseEntity.ok(ApiResponse.success(null,"Đăng xuất thành công"));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    /**
     * Get current user info
     * GET /api/v1/auth/me
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GUARD')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserAuthInfo>> getCurrentUser() {
        User user = userService.getCurrentUser();
        LoginResponse.UserAuthInfo userInfo = LoginResponse.UserAuthInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .status(user.getStatus().toString())
                .phone(user.getPhone())
                .build();
        return ResponseEntity.ok(ApiResponse.success(userInfo,"Lấy thông tin người dùng thành công"));
    }

    @SecurityRequirements
    @Operation(summary = "Request OTP for password reset")
    @PostMapping("forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody PhoneRequest request) {
        log.info("Password reset requested for email: {}", request.getPhone());
        authService.forgotPasswordHandler(request);
        return ResponseEntity.ok(ApiResponse.success(request.getPhone(),"Mã otp đã được gửi đến số điện thoại của bạn"));
    }
    @SecurityRequirements
    @Operation(summary = "Verify OTP")
    @PostMapping("verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        String resetToken =  authService.otpVerifyHandler(request);
        return ResponseEntity.ok(ApiResponse.success(resetToken,"Xác thực OTP thành công, bạn có thể đặt lại mật khẩu của mình"));
    }

    @SecurityRequirements
    @Operation(summary = "Reset password")
    @PostMapping("reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        authService.resetPasswordHandler(request);
        return ResponseEntity.ok(ApiResponse.success(null,"Đặt lại mật khẩu thành công"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GUARD')")
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword( @Valid @RequestBody
            ChangePasswordRequest request) {
        userService.changePasswordHandler(request);
        return ResponseEntity.ok(ApiResponse.success(null,"Đổi mật khẩu thành công"));
    }

}
