package smartparkingsystem.backend.controller.v1.auth;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.auth.LoginRequest;
import smartparkingsystem.backend.dto.request.auth.OtpVerifyRequest;
import smartparkingsystem.backend.dto.request.auth.RefreshTokenRequest;
import smartparkingsystem.backend.dto.request.PhoneRequest;
import smartparkingsystem.backend.dto.request.auth.ResetPasswordRequest;
import smartparkingsystem.backend.dto.response.LoginResponse;
import smartparkingsystem.backend.service.auth.AuthService;

import smartparkingsystem.backend.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    /**
     * Login endpoint for ADMIN users
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        log.info("User logged in successfully: {}", loginRequest.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response,"Login successful"));
    }

    /**
     * Refresh access token endpoint
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Token refresh requested");
        LoginResponse response = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(response,"Refresh Token successful"));
    }

    /**
     * Logout endpoint
     * POST /api/v1/auth/logout
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GUARD')")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            log.info("Logout requested for user: {}", username);
            authService.logout(username);
            log.info("User logged out successfully: {}", username);
        }
        return ResponseEntity.ok(ApiResponse.success(null,"Logout successful"));
    }

    /**
     * Get current user info
     * GET /api/v1/auth/me
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GUARD')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserAuthInfo>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        LoginResponse.UserAuthInfo userInfo = new LoginResponse.UserAuthInfo();
        userInfo.setUsername(authentication.getName());
        userInfo.setRole(authentication.getAuthorities().toString());

        return ResponseEntity.ok(ApiResponse.success(userInfo,"Current user info retrieved successfully"));
    }

    @PostMapping("forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody PhoneRequest request) {
        log.info("Password reset requested for email: {}", request.getPhone());
        authService.forgotPasswordHandler(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success(request.getPhone(),"Mã otp đã được gửi đến số điện thoại của bạn"));
    }
    @PostMapping("verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        String resetToken =  authService.otpVerifyHandler(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success(resetToken,"OTP verified successfully. start resetting password"));
    }

    @PostMapping("reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        authService.resetPasswordHandler(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null,"Password reset successful"));
    }
}

