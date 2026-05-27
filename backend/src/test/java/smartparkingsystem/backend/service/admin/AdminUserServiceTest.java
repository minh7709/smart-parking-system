package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.dto.request.user.UserCreateRequest;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.service.auth.TokenRedisService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private TokenRedisService tokenRedisService;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void createUser_duplicateUsername_throwException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin");
        request.setPhone("0912345678");

        when(userRepository.existsByUsernameAndDeletedFalse("admin")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminUserService.createUser(request));
    }

    @Test
    void deleteUser_shouldInvalidateTokens() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setDeleted(false);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        adminUserService.deleteUser(userId);

        verify(tokenRedisService).markUserAsDeleted(userId);
        verify(userRepository).save(user);
    }
}
