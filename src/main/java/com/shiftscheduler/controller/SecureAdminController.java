package com.shiftscheduler.controller;

import com.shiftscheduler.dto.CreateUserRequest;
import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import com.shiftscheduler.util.PasswordHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/secure-admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SecureAdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdminUser(@RequestBody CreateUserRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User with this email already exists"));
        }

        try {
            // Create new admin user
            User admin = new User();
            admin.setEmail(request.getEmail());

            // Hash password using SHA256_BCRYPT method
            String sha256Hash = PasswordHashUtil.hashWithSHA256(request.getPassword());
            admin.setPassword(passwordEncoder.encode(sha256Hash));

            admin.setFirstName(request.getFirstName());
            admin.setLastName(request.getLastName());
            admin.setActive(true);
            admin.setFirstTimeLogin(true); // Force password change for security
            admin.setPasswordHashMethod("SHA256_BCRYPT");

            User savedAdmin = userRepository.save(admin);

            // Assign ADMIN role
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            UserRole userRole = new UserRole();
            userRole.setUser(savedAdmin);
            userRole.setRole(adminRole);
            userRoleRepository.save(userRole);

            return ResponseEntity.ok(Map.of(
                    "message", "Admin user created successfully",
                    "email", savedAdmin.getEmail(),
                    "id", savedAdmin.getId(),
                    "firstTimeLogin", savedAdmin.isFirstTimeLogin()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create admin user: " + e.getMessage()));
        }
    }

    @GetMapping("/check-admin-exists")
    public ResponseEntity<?> checkAdminExists() {
        // Get admin email from environment variable (required, no default)
        String adminEmail = System.getenv("ADMIN_EMAIL");
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "adminExists", false,
                    "adminEmail", "NOT_SET",
                    "message", "ADMIN_EMAIL environment variable not set - no admin user configured"
            ));
        }

        Optional<User> admin = userRepository.findByEmail(adminEmail);
        return ResponseEntity.ok(Map.of(
                "adminExists", admin.isPresent(),
                "adminEmail", adminEmail,
                "message", admin.isPresent() ?
                        "Admin user exists with email: " + adminEmail :
                        "No admin user found with email: " + adminEmail + " - set ADMIN_PASSWORD and ADMIN_EMAIL environment variables and restart"
        ));
    }
}
