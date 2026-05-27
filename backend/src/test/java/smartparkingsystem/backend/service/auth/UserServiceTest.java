package smartparkingsystem.backend.service.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.exception.UnauthorizedException;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.security.CustomUserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_noAuth_throwUnauthorized() {
        SecurityContextHolder.clearContext();
        assertThrows(UnauthorizedException.class, () -> userService.getCurrentUser());
    }

    @Test
    void changePasswordHandler_wrongCurrentPassword_throwUnauthorized() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("guard");
        user.setRole(UserRole.GUARD);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword("encoded");

        CustomUserDetails principal = new CustomUserDetails(userId, "guard", "encoded", UserRole.GUARD, true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        smartparkingsystem.backend.dto.request.auth.ChangePasswordRequest request =
                new smartparkingsystem.backend.dto.request.auth.ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("NewPass123");

        assertThrows(UnauthorizedException.class, () -> userService.changePasswordHandler(request));
    }
}
