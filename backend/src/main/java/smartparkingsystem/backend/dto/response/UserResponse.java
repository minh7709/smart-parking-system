package smartparkingsystem.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
