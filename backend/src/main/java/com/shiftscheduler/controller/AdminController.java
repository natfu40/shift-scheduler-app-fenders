package com.shiftscheduler.controller;

import com.shiftscheduler.dto.AdminResponse;
import com.shiftscheduler.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/users/{userId}/make-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> makeUserAdmin(@PathVariable Long userId) {
        AdminResponse response = adminService.assignAdminRole(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/remove-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> removeAdminFromUser(@PathVariable Long userId) {
        AdminResponse response = adminService.removeAdminRole(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/is-admin")
    public ResponseEntity<Boolean> isUserAdmin(@PathVariable Long userId) {
        boolean isAdmin = adminService.isUserAdmin(userId);
        return ResponseEntity.ok(isAdmin);
    }
}

