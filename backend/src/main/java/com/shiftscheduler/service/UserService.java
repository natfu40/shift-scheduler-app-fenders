package com.shiftscheduler.service;

import com.shiftscheduler.dto.UserDTO;
import com.shiftscheduler.mapper.DTOMapper;
import com.shiftscheduler.repository.AuditLogRepository;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
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

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(dtoMapper::toUserDTO)
                .collect(Collectors.toList());
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

