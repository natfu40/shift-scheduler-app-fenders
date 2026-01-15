package com.shiftscheduler.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User not found with id: " + id);
    }

    public static ResourceNotFoundException shift(Long id) {
        return new ResourceNotFoundException("Shift not found with id: " + id);
    }

    public static ResourceNotFoundException assignment(Long id) {
        return new ResourceNotFoundException("Assignment not found with id: " + id);
    }
}
