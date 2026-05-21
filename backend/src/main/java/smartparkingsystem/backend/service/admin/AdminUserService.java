package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.dto.request.user.UserCreateRequest;
import smartparkingsystem.backend.dto.request.user.UserUpdateRequest;
import smartparkingsystem.backend.dto.response.UserResponse;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.service.auth.TokenRedisService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRedisService tokenRedisService;

    public UserResponse createUser(UserCreateRequest request) {
        validateUniqueForCreate(request.getUsername(), request.getPhone());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setDeleted(false);

        User saved = userRepository.save(user);
        log.info("Created user with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user được chọn"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(UserStatus status, String phone) {
        String trimmedPhone = phone != null ? phone.trim() : null;

        boolean hasPhone = trimmedPhone != null && !trimmedPhone.isBlank();
        boolean hasStatus = status != null;

        List<User> users;

        if (hasPhone && hasStatus) {
            users = userRepository.findByPhoneContainingAndStatusAndDeletedFalse(trimmedPhone, status);
        } else if (hasPhone) {
            users = userRepository.findByPhoneContainingAndDeletedFalse(trimmedPhone);
        } else if (hasStatus) {
            users = userRepository.findByStatusAndDeletedFalse(status);
        } else {
            users = userRepository.findAllByDeletedFalse();
        }

        return users.stream()
                .map(this::toResponse)
                .toList();
    }
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User existing = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng được chọn"));
        validateUniqueForUpdate(request.getUsername(), request.getPhone(), id);

        existing.setUsername(request.getUsername());
        existing.setFullName(request.getFullName());
        existing.setPhone(request.getPhone());
        existing.setStatus(request.getStatus());

        User updated = userRepository.save(existing);
        log.info("Updated user with id: {}", id);
        return toResponse(updated);
    }
    @Transactional
    public void deleteUser(UUID id) {
        User existing = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng được chọn"));

        existing.setDeleted(true);
        userRepository.save(existing);

        // Vô hiệu hóa tất cả token của user này
        tokenRedisService.markUserAsDeleted(id);
        log.info("Soft deleted user with id: {} and invalidated all tokens", id);
    }

    private void validateUniqueForCreate(String username, String phone) {
        if (userRepository.existsByUsernameAndDeletedFalse(username)) {
            throw new DuplicateResourceException("User với username '" + username + "' đã tồn tại");
        }
        if (userRepository.existsByPhoneAndDeletedFalse(phone)) {
            throw new DuplicateResourceException("User với số điện thoại '" + phone + "' đã tồn tại");
        }
    }

    private void validateUniqueForUpdate(String username, String phone, UUID id) {
        if (userRepository.existsByUsernameAndDeletedFalseAndIdNot(username, id)) {
            throw new DuplicateResourceException("User với username '" + username + "' đã tồn tại");
        }
        if (userRepository.existsByPhoneAndDeletedFalseAndIdNot(phone, id)) {
            throw new DuplicateResourceException("User với số điện thoại '" + phone + "' đã tồn tại");
        }
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
