package smartparkingsystem.backend.controller.v1.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * Admin controller - Only ADMIN users can access these endpoints
 * This is protected by @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserRepository userRepository;

}

