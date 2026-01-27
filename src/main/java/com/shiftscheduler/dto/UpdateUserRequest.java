package com.shiftscheduler.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private Boolean active;
}
