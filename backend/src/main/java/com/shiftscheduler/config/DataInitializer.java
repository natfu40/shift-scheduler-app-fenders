package com.shiftscheduler.config;

import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import com.shiftscheduler.util.PasswordHashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initializeData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Create default roles if they don't exist
            createRoleIfNotExists(roleRepository, "ADMIN", "Administrator role with full access");
            createRoleIfNotExists(roleRepository, "USER", "Regular user role");

            // Check if demo admin user exists
            Optional<User> adminUser = userRepository.findByEmail("admin@example.com");
            if (adminUser.isEmpty()) {
                // Create a demo admin user with SHA256_BCRYPT password method
                User admin = new User();
                admin.setEmail("admin@example.com");

                // Hash the default password with SHA256 first, then bcrypt it
                String defaultPassword = "admin123";
                String sha256Hash = PasswordHashUtil.hashWithSHA256(defaultPassword);
                admin.setPassword(passwordEncoder.encode(sha256Hash));

                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setActive(true);
                admin.setFirstTimeLogin(false); // Demo admin doesn't need to change password
                admin.setPasswordHashMethod("SHA256_BCRYPT"); // Use new secure method
                User savedAdmin = userRepository.save(admin);

                // Assign ADMIN role to the new user
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                UserRole userRole = new UserRole();
                userRole.setUser(savedAdmin);
                userRole.setRole(adminRole);
                userRoleRepository.save(userRole);

                logger.info("Demo admin user created with email: admin@example.com");
            } else {
                // Admin user exists - check if it needs migration to SHA256_BCRYPT
                User existingAdmin = adminUser.get();
                if (!"SHA256_BCRYPT".equals(existingAdmin.getPasswordHashMethod())) {
                    // Migrate existing admin to new password method
                    String defaultPassword = "admin123";
                    String sha256Hash = PasswordHashUtil.hashWithSHA256(defaultPassword);
                    existingAdmin.setPassword(passwordEncoder.encode(sha256Hash));
                    existingAdmin.setPasswordHashMethod("SHA256_BCRYPT");
                    userRepository.save(existingAdmin);

                    logger.info("Migrated existing admin user to SHA256_BCRYPT method");
                } else {
                    logger.info("Admin user already using SHA256_BCRYPT method");
                }

                // Ensure admin user has ADMIN role (in case it was missing)
                try {
                    Role adminRole = roleRepository.findByName("ADMIN")
                            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

                    logger.debug("Found ADMIN role with ID: {}", adminRole.getId());
                    logger.debug("Checking if admin user (ID: {}) has ADMIN role", existingAdmin.getId());

                    List<UserRole> existingUserRoles = userRoleRepository.findByUserId(existingAdmin.getId());
                    logger.debug("Admin user has {} roles", existingUserRoles.size());
                    for (UserRole ur : existingUserRoles) {
                        logger.debug("Role: {} (ID: {})", ur.getRole().getName(), ur.getRole().getId());
                    }

                    boolean hasAdminRole = existingUserRoles.stream()
                            .anyMatch(ur -> ur.getRole().getName().equals("ADMIN"));

                    logger.debug("Admin user hasAdminRole: {}", hasAdminRole);

                    if (!hasAdminRole) {
                        // First, try to delete any existing role assignments to avoid conflicts
                        userRoleRepository.deleteByUserId(existingAdmin.getId());

                        // Now assign the ADMIN role
                        UserRole userRole = new UserRole();
                        userRole.setUser(existingAdmin);
                        userRole.setRole(adminRole);
                        UserRole saved = userRoleRepository.save(userRole);
                        logger.info("Assigned ADMIN role to existing admin user (UserRole ID: {})", saved.getId());
                    } else {
                        logger.info("Admin user already has ADMIN role");
                    }
                } catch (Exception e) {
                    logger.error("Error assigning admin role: {}", e.getMessage(), e);

                    // Try a simpler approach - just create the role assignment directly
                    try {
                        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
                        if (adminRole != null) {
                            UserRole userRole = new UserRole();
                            userRole.setUser(existingAdmin);
                            userRole.setRole(adminRole);
                            userRoleRepository.save(userRole);
                            logger.warn("Force-assigned ADMIN role to admin user");
                        }
                    } catch (Exception e2) {
                        logger.error("Failed to force-assign admin role: {}", e2.getMessage(), e2);
                    }
                }
            }
        };
    }

    private void createRoleIfNotExists(RoleRepository roleRepository, String roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            logger.info("Created role: {}", roleName);
        }
    }
}
