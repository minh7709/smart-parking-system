package smartparkingsystem.backend.controller.v1.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller - Only ADMIN users can access these endpoints
 * This is protected by @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/api/v1/admin")
@AllArgsConstructor
@Slf4j
public class AdminController {
    private final UserRepository userRepository;

    /**
     * Get all users - ADMIN only
     * GET /api/v1/admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Admin fetching all users");
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID - ADMIN only
     * GET /api/v1/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        log.info("Admin fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get admin dashboard info - ADMIN only
     * GET /api/v1/admin/dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = authentication.getName();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("adminUser", adminUsername);
        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("timestamp", java.time.LocalDateTime.now());
        dashboard.put("message", "Welcome to Smart Parking Admin Dashboard");

        log.info("Admin dashboard accessed by user: {}", adminUsername);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Check admin permissions - ADMIN only
     * GET /api/v1/admin/verify-access
     */
    @GetMapping("/verify-access")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());

        return ResponseEntity.ok(response);
    }
}

