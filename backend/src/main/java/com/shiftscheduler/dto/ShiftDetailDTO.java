package com.shiftscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDetailDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int availableSlots;
    private int filledSlots;
    private boolean active;
    private Long createdById;
    private String createdByName;
    private List<ShiftAssignmentDTO> signups;
}

