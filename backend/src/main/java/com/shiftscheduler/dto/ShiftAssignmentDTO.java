package com.shiftscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private Instant signedUpAt;
    private Instant acceptedAt;
}

