package com.shiftscheduler.config;

import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import com.shiftscheduler.util.PasswordHashUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            // Only create admin user if it doesn't exist (security: don't recreate)
            // Get admin email from environment variable (required)
            String adminEmail = System.getenv("ADMIN_EMAIL");
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                logger.warn("ADMIN_EMAIL environment variable not set. Skipping admin user creation.");
                logger.warn("To create admin user, set both ADMIN_EMAIL and ADMIN_PASSWORD environment variables and restart.");
                return; // Skip admin creation if no email provided
            }

            // Get admin password from environment variable
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            if (adminPassword == null || adminPassword.trim().isEmpty()) {
                logger.warn("ADMIN_PASSWORD environment variable not set. Skipping admin user creation.");
                logger.warn("To create admin user, set both ADMIN_EMAIL and ADMIN_PASSWORD environment variables and restart.");
                return;
            }

            // Hash the admin password: SHA256(adminPassword) -> BCrypt(SHA256_hash)
            String sha256Hash = PasswordHashUtil.hashWithSHA256(adminPassword);
            String encodedPassword = passwordEncoder.encode(sha256Hash);

            Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
            if (existingAdmin.isEmpty()) {
                logger.info("Creating initial admin user...");
                logger.info("Admin email: {}", adminEmail);

                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(encodedPassword);
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setActive(true);
                admin.setFirstTimeLogin(true);
                admin.setPasswordHashMethod("SHA256_BCRYPT");
                User savedAdmin = userRepository.save(admin);

                // Assign ADMIN role to the new user
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                UserRole userRole = new UserRole();
                userRole.setUser(savedAdmin);
                userRole.setRole(adminRole);
                userRoleRepository.save(userRole);

                logger.info("✅ Initial admin user created successfully with email: {}", adminEmail);
                logger.info("⚠️ Admin must change password on first login for security");
            } else {
                // User already exists — ensure password and method are correct (fixes stale/broken records)
                User admin = existingAdmin.get();
                boolean needsUpdate = false;

                if (!"SHA256_BCRYPT".equals(admin.getPasswordHashMethod())) {
                    logger.warn("Admin user exists but has wrong password method ({}). Fixing...", admin.getPasswordHashMethod());
                    admin.setPasswordHashMethod("SHA256_BCRYPT");
                    needsUpdate = true;
                }

                if (!passwordEncoder.matches(sha256Hash, admin.getPassword())) {
                    logger.warn("Admin user exists but password does not match env var. Resetting password...");
                    admin.setPassword(encodedPassword);
                    admin.setFirstTimeLogin(true);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    userRepository.save(admin);
                    logger.info("✅ Admin user updated for email: {}", adminEmail);
                } else {
                    logger.info("Admin user already exists and is correctly configured: {}", adminEmail);
                }

                // Ensure ADMIN role is assigned
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                boolean hasAdminRole = userRoleRepository.findByUserId(admin.getId())
                        .stream().anyMatch(ur -> "ADMIN".equals(ur.getRole().getName()));
                if (!hasAdminRole) {
                    logger.warn("Admin user missing ADMIN role. Assigning...");
                    UserRole userRole = new UserRole();
                    userRole.setUser(admin);
                    userRole.setRole(adminRole);
                    userRoleRepository.save(userRole);
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
