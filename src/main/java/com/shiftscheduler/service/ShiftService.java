package com.shiftscheduler.service;

import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.ShiftDetailDTO;
import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.mapper.DTOMapper;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AuditLogService auditLogService;
    private final DTOMapper dtoMapper;

    @Transactional
    public ShiftDTO createShift(ShiftDTO shiftDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        Shift shift = buildShiftFromDTO(shiftDTO, user);
        Shift savedShift = shiftRepository.save(shift);

        auditLogService.logAction(user, "CREATE_SHIFT", "Shift", savedShift.getId(),
                "Created shift: " + savedShift.getName());

        return dtoMapper.toShiftDTO(savedShift);
    }

    @Transactional
    public ShiftDTO updateShift(Long shiftId, ShiftDTO shiftDTO, Long userId) {
        Shift shift = findShiftById(shiftId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        updateShiftFromDTO(shift, shiftDTO);
        Shift updatedShift = shiftRepository.save(shift);

        auditLogService.logAction(user, "UPDATE_SHIFT", "Shift", shiftId,
                "Updated shift: " + updatedShift.getName());

        return dtoMapper.toShiftDTO(updatedShift);
    }

    @Transactional
    public void deleteShift(Long shiftId, Long userId) {
        Shift shift = findShiftById(shiftId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        String shiftName = shift.getName();

        // First, delete all shift assignments for this shift to avoid foreign key constraint violations
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftId(shiftId);
        if (!assignments.isEmpty()) {
            auditLogService.logAction(user, "DELETE_SHIFT_ASSIGNMENTS", "ShiftAssignment", shiftId,
                    "Deleted " + assignments.size() + " assignment(s) for shift: " + shiftName);
            shiftAssignmentRepository.deleteByShiftId(shiftId);
        }

        // Now delete the shift itself
        shiftRepository.delete(shift);

        auditLogService.logAction(user, "DELETE_SHIFT", "Shift", shiftId,
                "Deleted shift: " + shiftName);
    }

    public ShiftDTO getShiftById(Long shiftId) {
        return dtoMapper.toShiftDTO(findShiftById(shiftId));
    }

    public Page<ShiftDTO> getAvailableShifts(Pageable pageable) {
        return shiftRepository.findByActiveTrue(pageable).map(dtoMapper::toShiftDTO);
    }

    public Page<ShiftDTO> getUpcomingShifts(Pageable pageable) {
        return shiftRepository.findByActiveTrueAndStartTimeAfter(LocalDateTime.now(), pageable)
                .map(dtoMapper::toShiftDTO);
    }

    public Page<ShiftDTO> getShiftsByUser(Long userId, Pageable pageable) {
        List<ShiftDTO> allShifts = shiftRepository.findByCreatedById(userId).stream()
                .map(dtoMapper::toShiftDTO)
                .collect(Collectors.toList());

        return createPageFromList(allShifts, pageable);
    }
    public ShiftDetailDTO getShiftDetails(Long shiftId) {
        Shift shift = findShiftById(shiftId);

        ShiftDetailDTO detailDTO = createShiftDetailDTO(shift);
        List<ShiftAssignmentDTO> signups = getShiftSignups(shiftId);
        detailDTO.setSignups(signups);

        return detailDTO;
    }

    // Helper methods
    private Shift findShiftById(Long shiftId) {
        return shiftRepository.findById(shiftId)
                .orElseThrow(() -> ResourceNotFoundException.shift(shiftId));
    }

    private Shift buildShiftFromDTO(ShiftDTO shiftDTO, User creator) {
        Shift shift = new Shift();
        shift.setName(shiftDTO.getName());
        shift.setDescription(shiftDTO.getDescription());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setAvailableSlots(shiftDTO.getAvailableSlots());
        shift.setCreatedBy(creator);
        shift.setActive(true);
        return shift;
    }

    private void updateShiftFromDTO(Shift shift, ShiftDTO shiftDTO) {
        shift.setName(shiftDTO.getName());
        shift.setDescription(shiftDTO.getDescription());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setAvailableSlots(shiftDTO.getAvailableSlots());
        shift.setActive(shiftDTO.isActive());
        // Do NOT update filledSlots - it's managed through signup/rejection only
    }

    private ShiftDetailDTO createShiftDetailDTO(Shift shift) {
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
        return detailDTO;
    }

    private List<ShiftAssignmentDTO> getShiftSignups(Long shiftId) {
        return shiftAssignmentRepository.findByShiftId(shiftId)
                .stream()
                .map(dtoMapper::toAssignmentDTO)
                .collect(Collectors.toList());
    }

    private Page<ShiftDTO> createPageFromList(List<ShiftDTO> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());

        // Handle edge case where start index is beyond available data
        if (start >= items.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, items.size());
        }

        List<ShiftDTO> paginatedItems = items.subList(start, end);
        return new PageImpl<>(paginatedItems, pageable, items.size());
    }
}

