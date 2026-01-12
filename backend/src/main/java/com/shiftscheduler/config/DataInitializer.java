package com.shiftscheduler.config;

import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer {

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
                // Create a demo admin user
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // Change this password!
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setActive(true);
                User savedAdmin = userRepository.save(admin);

                // Assign ADMIN role to the new user
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                UserRole userRole = new UserRole();
                userRole.setUser(savedAdmin);
                userRole.setRole(adminRole);
                userRoleRepository.save(userRole);

                System.out.println("✓ Demo admin user created with email: admin@example.com and password: admin123");
                System.out.println("⚠️  IMPORTANT: Change this password immediately in production!");
            }
        };
    }

    private void createRoleIfNotExists(RoleRepository roleRepository, String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            System.out.println("✓ Role created: " + name);
        }
    }
}

