package com.shiftscheduler.controller;

import com.shiftscheduler.dto.AuthResponse;
import com.shiftscheduler.dto.ChangePasswordRequest;
import com.shiftscheduler.dto.ChangePasswordHashedRequest;
import com.shiftscheduler.dto.LoginRequest;
import com.shiftscheduler.dto.SignupRequest;
import com.shiftscheduler.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.signup(signupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-hashed")
    public ResponseEntity<AuthResponse> loginWithHashedPassword(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.loginWithHashedPassword(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Authentication authentication) {
        authService.changePassword(changePasswordRequest, authentication.getName());
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/change-password-hashed")
    public ResponseEntity<String> changePasswordHashed(@RequestBody ChangePasswordHashedRequest changePasswordRequest, Authentication authentication) {
        authService.changePasswordHashed(changePasswordRequest, authentication.getName());
        return ResponseEntity.ok("Password changed successfully");
    }
}

