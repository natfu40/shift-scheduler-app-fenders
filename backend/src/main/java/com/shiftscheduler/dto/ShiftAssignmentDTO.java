package com.shiftscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignmentDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private Long shiftId;
    private String shiftName;
    private boolean accepted;
    private LocalDateTime signedUpAt;
    private LocalDateTime acceptedAt;
}

