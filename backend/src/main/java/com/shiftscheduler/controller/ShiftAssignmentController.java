package com.shiftscheduler.controller;

import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.service.ShiftAssignmentService;
import com.shiftscheduler.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shift-assignments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShiftAssignmentController {

    @Autowired
    private ShiftAssignmentService shiftAssignmentService;

    @PostMapping("/signup/{shiftId}")
    public ResponseEntity<ShiftAssignmentDTO> signupForShift(@PathVariable Long shiftId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ShiftAssignmentDTO response = shiftAssignmentService.signupForShift(shiftId, userPrincipal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{assignmentId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftAssignmentDTO> acceptSignup(@PathVariable Long assignmentId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ShiftAssignmentDTO response = shiftAssignmentService.acceptSignup(assignmentId, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{assignmentId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectSignup(@PathVariable Long assignmentId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        shiftAssignmentService.rejectSignup(assignmentId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shift/{shiftId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ShiftAssignmentDTO>> getSignupsForShift(@PathVariable Long shiftId) {
        List<ShiftAssignmentDTO> response = shiftAssignmentService.getSignupsForShift(shiftId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ShiftAssignmentDTO>> getUserShiftAssignments(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<ShiftAssignmentDTO> response = shiftAssignmentService.getUserShiftAssignments(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }
}

