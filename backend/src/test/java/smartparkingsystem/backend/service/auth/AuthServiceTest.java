package smartparkingsystem.backend.service.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import smartparkingsystem.backend.config.JwtProperties;
import smartparkingsystem.backend.dto.request.auth.LoginRequest;
import smartparkingsystem.backend.dto.request.auth.RefreshTokenRequest;
import smartparkingsystem.backend.exception.UnauthorizedException;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.security.JwtTokenProvider;
import smartparkingsystem.backend.service.thirdService.SmsService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private OtpRedisService otpRedisService;

    @Mock
    private SmsService smsService;

    @Mock
    private UserService userService;

    @Mock
    private TokenRedisService tokenRedisService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_badCredentials_throwUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_inactive_throwUnauthorized() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh");

        when(tokenRedisService.isRefreshTokenActive("refresh")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }
}
