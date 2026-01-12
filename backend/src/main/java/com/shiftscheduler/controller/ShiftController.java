package com.shiftscheduler.controller;

import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.ShiftDetailDTO;
import com.shiftscheduler.service.ShiftService;
import com.shiftscheduler.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftDTO> createShift(@RequestBody ShiftDTO shiftDTO, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ShiftDTO response = shiftService.createShift(shiftDTO, userPrincipal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{shiftId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftDTO> updateShift(@PathVariable Long shiftId, @RequestBody ShiftDTO shiftDTO, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ShiftDTO response = shiftService.updateShift(shiftId, shiftDTO, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shiftId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShift(@PathVariable Long shiftId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        shiftService.deleteShift(shiftId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shiftId}")
    public ResponseEntity<ShiftDTO> getShift(@PathVariable Long shiftId) {
        ShiftDTO response = shiftService.getShiftById(shiftId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shiftId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftDetailDTO> getShiftDetails(@PathVariable Long shiftId) {
        ShiftDetailDTO response = shiftService.getShiftDetails(shiftId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<ShiftDTO>> getAvailableShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftDTO> response = shiftService.getAvailableShifts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<ShiftDTO>> getUpcomingShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftDTO> response = shiftService.getUpcomingShifts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ShiftDTO>> getShiftsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftDTO> response = shiftService.getShiftsByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }
}

