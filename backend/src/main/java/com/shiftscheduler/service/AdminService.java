package com.shiftscheduler.service;

import com.shiftscheduler.dto.AdminResponse;
import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public AdminResponse assignAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has ADMIN role
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        boolean hasAdminRole = existingRoles.stream()
                .anyMatch(ur -> ur.getRole().getName().equals("ADMIN"));

        if (hasAdminRole) {
            return new AdminResponse(userId, user.getEmail(), "User already has ADMIN role", true);
        }

        // Get or create ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newAdminRole = new Role();
                    newAdminRole.setName("ADMIN");
                    newAdminRole.setDescription("Administrator role with full access");
                    return roleRepository.save(newAdminRole);
                });

        // Assign ADMIN role to user
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(adminRole);
        userRoleRepository.save(userRole);

        return new AdminResponse(userId, user.getEmail(), "User promoted to ADMIN", true);
    }

    public AdminResponse removeAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find and remove ADMIN role
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        UserRole adminRole = userRoles.stream()
                .filter(ur -> ur.getRole().getName().equals("ADMIN"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User does not have ADMIN role"));

        userRoleRepository.delete(adminRole);

        return new AdminResponse(userId, user.getEmail(), "ADMIN role removed from user", true);
    }

    public boolean isUserAdmin(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole().getName().equals("ADMIN"));
    }
}

