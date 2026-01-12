package com.shiftscheduler.service;

import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.ShiftDetailDTO;
import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private AuditLogService auditLogService;

    public ShiftDTO createShift(ShiftDTO shiftDTO, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Shift shift = new Shift();
        shift.setName(shiftDTO.getName());
        shift.setDescription(shiftDTO.getDescription());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setAvailableSlots(shiftDTO.getAvailableSlots());
        shift.setCreatedBy(user);
        shift.setActive(true);

        Shift savedShift = shiftRepository.save(shift);

        auditLogService.logAction(user, "CREATE_SHIFT", "Shift", savedShift.getId(),
                "Created shift: " + savedShift.getName());

        return convertToDTO(savedShift);
    }

    public ShiftDTO updateShift(Long shiftId, ShiftDTO shiftDTO, Long userId) {
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Shift not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        shift.setName(shiftDTO.getName());
        shift.setDescription(shiftDTO.getDescription());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setAvailableSlots(shiftDTO.getAvailableSlots());
        shift.setActive(shiftDTO.isActive());

        Shift updatedShift = shiftRepository.save(shift);

        auditLogService.logAction(user, "UPDATE_SHIFT", "Shift", shiftId,
                "Updated shift: " + updatedShift.getName());

        return convertToDTO(updatedShift);
    }

    public void deleteShift(Long shiftId, Long userId) {
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Shift not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        shiftRepository.delete(shift);

        auditLogService.logAction(user, "DELETE_SHIFT", "Shift", shiftId,
                "Deleted shift: " + shift.getName());
    }

    public ShiftDTO getShiftById(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Shift not found"));
        return convertToDTO(shift);
    }

    public Page<ShiftDTO> getAvailableShifts(Pageable pageable) {
        return shiftRepository.findByActiveTrue(pageable).map(this::convertToDTO);
    }

    public Page<ShiftDTO> getUpcomingShifts(Pageable pageable) {
        return shiftRepository.findByActiveTrueAndStartTimeAfter(LocalDateTime.now(), pageable)
                .map(this::convertToDTO);
    }

    public Page<ShiftDTO> getShiftsByUser(Long userId, Pageable pageable) {
        List<ShiftDTO> allShifts = shiftRepository.findByCreatedById(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allShifts.size());

        List<ShiftDTO> paginatedShifts = allShifts.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(paginatedShifts, pageable, allShifts.size());
    }

    public ShiftDetailDTO getShiftDetails(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Shift not found"));

        ShiftDetailDTO detailDTO = new ShiftDetailDTO();
        detailDTO.setId(shift.getId());
        detailDTO.setName(shift.getName());
        detailDTO.setDescription(shift.getDescription());
        detailDTO.setStartTime(shift.getStartTime());
        detailDTO.setEndTime(shift.getEndTime());
        detailDTO.setAvailableSlots(shift.getAvailableSlots());
        detailDTO.setFilledSlots(shift.getFilledSlots());
        detailDTO.setActive(shift.isActive());
        detailDTO.setCreatedById(shift.getCreatedBy().getId());
        detailDTO.setCreatedByName(shift.getCreatedBy().getFirstName() + " " + shift.getCreatedBy().getLastName());

        // Get all signups for this shift
        List<ShiftAssignmentDTO> signups = shiftAssignmentRepository.findByShiftId(shiftId)
                .stream()
                .map(this::convertAssignmentToDTO)
                .collect(Collectors.toList());
        detailDTO.setSignups(signups);

        return detailDTO;
    }

    private ShiftAssignmentDTO convertAssignmentToDTO(com.shiftscheduler.model.ShiftAssignment assignment) {
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

    private ShiftDTO convertToDTO(Shift shift) {
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
        dto.setCreatedByName(shift.getCreatedBy().getFirstName() + " " + shift.getCreatedBy().getLastName());
        return dto;
    }
}

