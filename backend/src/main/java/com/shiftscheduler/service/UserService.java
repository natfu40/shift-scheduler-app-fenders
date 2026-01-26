package com.shiftscheduler.service;

import com.shiftscheduler.dto.CreateUserRequest;
import com.shiftscheduler.dto.UserDTO;
import com.shiftscheduler.mapper.DTOMapper;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.AuditLogRepository;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRoleRepository userRoleRepository;
    private final DTOMapper dtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(dtoMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(createUserRequest.getEmail());
        // Password comes pre-hashed with SHA256 from frontend, so we bcrypt the hash
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setActive(true);
        user.setFirstTimeLogin(true); // Admin-created users must change password on first login
        user.setPasswordHashMethod("SHA256_BCRYPT"); // New users use the secure method

        User savedUser = userRepository.save(user);

        auditLogService.logAction(savedUser, "USER_CREATED", "User", savedUser.getId(),
                "User created by admin with email: " + savedUser.getEmail());

        return dtoMapper.toUserDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        // Delete all shift assignments for this user
        shiftAssignmentRepository.deleteByUserId(userId);

        // Delete all user roles for this user
        userRoleRepository.deleteByUserId(userId);

        // Delete all audit logs for this user
        auditLogRepository.deleteByUserId(userId);

        // Finally, delete the user
        userRepository.deleteById(userId);
    }
}

