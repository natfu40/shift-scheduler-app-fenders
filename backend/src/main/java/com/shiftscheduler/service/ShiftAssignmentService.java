package com.shiftscheduler.service;

import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.ShiftRepository;
import com.shiftscheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShiftAssignmentService {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public ShiftAssignmentDTO signupForShift(Long shiftId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Shift not found"));

        if (shiftAssignmentRepository.findByUserIdAndShiftId(userId, shiftId).isPresent()) {
            throw new RuntimeException("User already signed up for this shift");
        }

        if (shift.getFilledSlots() >= shift.getAvailableSlots()) {
            throw new RuntimeException("Shift is full");
        }

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setUser(user);
        assignment.setShift(shift);
        assignment.setAccepted(false);

        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(assignment);

        auditLogService.logAction(user, "SIGNUP_SHIFT", "ShiftAssignment", savedAssignment.getId(),
                "Signed up for shift: " + shift.getName());

        return convertToDTO(savedAssignment);
    }

    public ShiftAssignmentDTO acceptSignup(Long assignmentId, Long adminUserId) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User adminUser = userRepository.findById(adminUserId).orElseThrow(() -> new RuntimeException("Admin user not found"));

        assignment.setAccepted(true);
        assignment.setAcceptedAt(LocalDateTime.now());

        Shift shift = assignment.getShift();
        shift.setFilledSlots(shift.getFilledSlots() + 1);
        shiftRepository.save(shift);

        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);

        auditLogService.logAction(adminUser, "ACCEPT_SIGNUP", "ShiftAssignment", assignmentId,
                "Accepted signup for shift: " + shift.getName() + " for user: " + assignment.getUser().getEmail());

        return convertToDTO(updatedAssignment);
    }

    public void rejectSignup(Long assignmentId, Long adminUserId) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User adminUser = userRepository.findById(adminUserId).orElseThrow(() -> new RuntimeException("Admin user not found"));

        auditLogService.logAction(adminUser, "REJECT_SIGNUP", "ShiftAssignment", assignmentId,
                "Rejected signup for shift: " + assignment.getShift().getName() + " for user: " + assignment.getUser().getEmail());

        shiftAssignmentRepository.delete(assignment);
    }

    public List<ShiftAssignmentDTO> getSignupsForShift(Long shiftId) {
        return shiftAssignmentRepository.findByShiftId(shiftId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ShiftAssignmentDTO> getUserShiftAssignments(Long userId) {
        return shiftAssignmentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ShiftAssignmentDTO convertToDTO(ShiftAssignment assignment) {
        ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setUserId(assignment.getUser().getId());
        dto.setUserEmail(assignment.getUser().getEmail());
        dto.setUserFirstName(assignment.getUser().getFirstName());
        dto.setUserLastName(assignment.getUser().getLastName());
        dto.setShiftId(assignment.getShift().getId());
        dto.setShiftName(assignment.getShift().getName());
        dto.setAccepted(assignment.isAccepted());
        dto.setSignedUpAt(assignment.getSignedUpAt());
        dto.setAcceptedAt(assignment.getAcceptedAt());
        return dto;
    }
}

