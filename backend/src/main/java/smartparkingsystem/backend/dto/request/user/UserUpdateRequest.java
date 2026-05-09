package smartparkingsystem.backend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @NotBlank(message = "Phone must not be blank")
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone must contain 10 or 11 digits")
    private String phone;

    @NotNull(message = "Role must not be null")
    private UserRole role;

    @NotNull(message = "Status must not be null")
    private UserStatus status;
}

