package com.shiftscheduler.mapper;

import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.dto.UserDTO;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class DTOMapper {

    @Autowired
    @Lazy
    private com.shiftscheduler.service.UserService userService;

    public UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                userService.isAdmin(user.getId())
        );
    }

    public ShiftDTO toShiftDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setId(shift.getId());
        dto.setName(shift.getName());
        dto.setDescription(shift.getDescription());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        dto.setAvailableSlots(shift.getAvailableSlots());
        dto.setFilledSlots(shift.getFilledSlots());
        dto.setActive(shift.isActive());
        dto.setCreatedById(shift.getCreatedBy().getId());
        dto.setCreatedByName(getFullName(shift.getCreatedBy()));
        return dto;
    }

    public ShiftAssignmentDTO toAssignmentDTO(ShiftAssignment assignment) {
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

    private String getFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
