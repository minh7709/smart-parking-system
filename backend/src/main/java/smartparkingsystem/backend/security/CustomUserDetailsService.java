package smartparkingsystem.backend.security;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.repository.UserRepository;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("không tìm thấy người dùng với username: " + username));
        if(user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("Người dùng '" + username + "' không có quyền truy cập hệ thống");
        }
        return CustomUserDetails.build(user);
    }

    /**
     * Load user by ID - used for JWT token validation
     */
    public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UsernameNotFoundException("không tìm thấy người dùng với id: " + userId));
        if(user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("Người dùng với '" + userId + "' không có quyền truy cập hệ thống");
        }
        return CustomUserDetails.build(user);
    }
}

