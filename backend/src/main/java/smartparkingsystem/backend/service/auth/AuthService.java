package smartparkingsystem.backend.service.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import smartparkingsystem.backend.config.JwtProperties;
import smartparkingsystem.backend.dto.request.PhoneRequest;
import smartparkingsystem.backend.dto.request.auth.LoginRequest;
import smartparkingsystem.backend.dto.request.auth.OtpVerifyRequest;
import smartparkingsystem.backend.dto.request.auth.RefreshTokenRequest;
import smartparkingsystem.backend.dto.request.auth.ResetPasswordRequest;
import smartparkingsystem.backend.dto.response.LoginResponse;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.UnauthorizedException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.security.CustomUserDetails;
import smartparkingsystem.backend.security.JwtTokenProvider;
import smartparkingsystem.backend.service.thirdService.SmsService;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final OtpRedisService otpRedisService;
    private final SmsService smsService;
    private final UserService userService;
    private final TokenRedisService tokenRedisService;

    private static final String FORGOT_PASSWORD_PURPOSE = "FORGOT_PASSWORD";

    /**
     * Authenticate user and generate JWT tokens
     * Only ADMIN users can log in via web
     */
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user with username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Check user exists and is active BEFORE creating tokens
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("User account is not active");
            }

            String accessToken = tokenProvider.generateToken(authentication);

            // Generate refresh token with Remember Me option
            boolean rememberMe = loginRequest.getRememberMe() != null && loginRequest.getRememberMe();
            String refreshToken = tokenProvider.generateRefreshToken(
                    userDetails.getUsername(),
                    userDetails.getId(),
                    userDetails.getRole(),
                    rememberMe
            );

            long refreshTtl = rememberMe ? jwtProperties.getRememberMeExpiration() : jwtProperties.getRefreshExpiration();
            tokenRedisService.storeRefreshToken(refreshToken, refreshTtl);

            // Build response
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtProperties.getExpiration() / 1000) // Convert to seconds
                    .user(LoginResponse.UserAuthInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .fullName(user.getFullName())
                            .role(user.getRole().toString())
                            .status(user.getStatus().toString())
                            .rememberMe(rememberMe)
                            .build())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!tokenRedisService.isRefreshTokenActive(refreshToken)) {
            throw new UnauthorizedException("Refresh token has been revoked or does not exist");
        }

        // Validate refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            tokenRedisService.revokeRefreshToken(refreshToken);
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Verify it's actually a refresh token
        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new ValidationException("Token is not a refresh token");
        }

        // Extract user info from refresh token
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        String role = tokenProvider.getRoleFromToken(refreshToken);
        boolean rememberMe = tokenProvider.getRememberMeFromToken(refreshToken);

        // Generate new access token
        String newAccessToken = tokenProvider.generateToken(username, userId, role);

        // Get user details
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .user(LoginResponse.UserAuthInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .role(user.getRole().toString())
                        .status(user.getStatus().toString())
                        .rememberMe(rememberMe)
                        .build())
                .build();
    }

    /**
     * Logout user and revoke tokens immediately
     */
    public void logout(String accessToken, String refreshToken, String username) {
        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken) && !tokenProvider.isRefreshToken(accessToken)) {
            long ttlMillis = tokenProvider.getRemainingExpirationMillis(accessToken);
            tokenRedisService.blacklistAccessToken(accessToken, ttlMillis);
        }

        tokenRedisService.revokeRefreshToken(refreshToken);
        log.info("User logged out and tokens revoked: {}", username);
    }

    public void forgotPasswordHandler(PhoneRequest request) {
        User user  = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("User with this phone number not found"));
        String otp = otpRedisService.generateOtp(user.getPhone(), FORGOT_PASSWORD_PURPOSE);
        smsService.sendSms(request.getPhone(), "Mã OTP đặt lại mật khẩu của bạn là: " + otp);
    }

    public String otpVerifyHandler(OtpVerifyRequest otpVerifyRequest) {
        boolean isValid = otpRedisService.validateOtp(otpVerifyRequest.getPhone(), otpVerifyRequest.getOtp(), FORGOT_PASSWORD_PURPOSE);
        if (!isValid) {
            throw new ValidationException("Invalid or expired OTP");
        }
        String resetToken = otpRedisService.generateResetToken(otpVerifyRequest.getPhone());
        return resetToken;
    }
    public void resetPasswordHandler(ResetPasswordRequest resetPasswordRequest) {
        String identifier = otpRedisService.validateResetToken(resetPasswordRequest.getToken());
        if (identifier == null) {
            throw new ValidationException("Invalid or expired reset token");
        }
        User user = userRepository.findByPhone(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userService.resetPasswordById(user.getId(), resetPasswordRequest.getNewPassword());
        otpRedisService.deleteResetToken(resetPasswordRequest.getToken());
    }
}
