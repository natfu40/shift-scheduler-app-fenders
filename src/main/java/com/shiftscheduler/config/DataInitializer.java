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

            // Always create/recreate demo admin user with consistent SHA256_BCRYPT method
            // Delete existing admin if it exists (for clean start)
            Optional<User> existingAdmin = userRepository.findByEmail("admin@example.com");
            if (existingAdmin.isPresent()) {
                User adminToDelete = existingAdmin.get();
                // Delete user roles first
                userRoleRepository.deleteByUserId(adminToDelete.getId());
                // Delete user
                userRepository.delete(adminToDelete);
                logger.info("Deleted existing admin user for clean recreation");
            }

            // Create fresh admin user with SHA256_BCRYPT method
            User admin = new User();
            admin.setEmail("admin@example.com");

            // Hash the default password: SHA256("admin123") -> BCrypt(SHA256_hash)
            String defaultPassword = "admin123";
            String sha256Hash = PasswordHashUtil.hashWithSHA256(defaultPassword);
            admin.setPassword(passwordEncoder.encode(sha256Hash));

            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setActive(true);
            admin.setFirstTimeLogin(false); // Demo admin doesn't need to change password
            admin.setPasswordHashMethod("SHA256_BCRYPT"); // Always use this method
            User savedAdmin = userRepository.save(admin);

            // Assign ADMIN role to the new user
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            UserRole userRole = new UserRole();
            userRole.setUser(savedAdmin);
            userRole.setRole(adminRole);
            userRoleRepository.save(userRole);

            logger.info("Created fresh demo admin user with email: admin@example.com using SHA256_BCRYPT method");
            logger.info("Admin password: SHA256('admin123') = {} -> BCrypt", sha256Hash);
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
