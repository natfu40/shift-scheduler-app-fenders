package com.shiftscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordHashedRequest {
    private String currentPassword; // SHA-256 hashed
    private String newPassword;     // SHA-256 hashed
}
