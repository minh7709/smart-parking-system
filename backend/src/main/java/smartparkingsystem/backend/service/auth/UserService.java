package smartparkingsystem.backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.UnauthorizedException;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.security.CustomUserDetails;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get currently logged-in user
     * @return User entity of the authenticated user
     * @throws UnauthorizedException if user is not authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("Invalid authentication principal");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found in database: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        log.debug("Retrieved current user: {}", user.getUsername());
        return user;
    }

    @Transactional
    public void resetPasswordById(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword)); // Password should be encoded before calling this method
        userRepository.save(user);
    }
}
