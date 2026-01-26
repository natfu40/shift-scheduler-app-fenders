package com.shiftscheduler.service;

import com.shiftscheduler.dto.ChangePasswordRequest;
import com.shiftscheduler.dto.ChangePasswordHashedRequest;
import com.shiftscheduler.dto.LoginRequest;
import com.shiftscheduler.dto.SignupRequest;
import com.shiftscheduler.dto.AuthResponse;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.security.JwtTokenProvider;
import com.shiftscheduler.security.UserPrincipal;
import com.shiftscheduler.util.PasswordHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = buildUserFromSignupRequest(signupRequest);
        userRepository.save(user);

        auditLogService.logAction(user, "SIGNUP", "User", user.getId(),
                "User registered with email: " + user.getEmail());

        return login(new LoginRequest(signupRequest.getEmail(), signupRequest.getPassword()));
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(userPrincipal.getId()));

        auditLogService.logAction(user, "LOGIN", "User", user.getId(), "User logged in");

        return new AuthResponse(
                token,
                "Bearer",
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isFirstTimeLogin()
        );
    }

    @Transactional
    public AuthResponse loginWithHashedPassword(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        boolean passwordMatches = false;

        // Check password based on storage method
        if ("SHA256_BCRYPT".equals(user.getPasswordHashMethod())) {
            // Password was stored as bcrypt(sha256(originalPassword))
            passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        } else {
            // Legacy user with BCRYPT storage - need to migrate
            // The frontend sent SHA256(originalPassword), but user has bcrypt(originalPassword)
            // We need to check if the original password would generate the same SHA256 hash
            // This is complex, so for now we'll create an error asking user to reset password
            throw new BadCredentialsException("Account needs password migration. Please contact administrator.");
        }

        if (!passwordMatches) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Create token and return auth response
        UserPrincipal userPrincipal = UserPrincipal.create(user, java.util.Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        String token = tokenProvider.generateToken(authentication);

        auditLogService.logAction(user, "LOGIN", "User", user.getId(), "User logged in (hashed)");

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isFirstTimeLogin()
        );
    }

    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password and mark as no longer first-time login
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setFirstTimeLogin(false);
        userRepository.save(user);

        auditLogService.logAction(user, "PASSWORD_CHANGE", "User", user.getId(), "Password changed");
    }

    @Transactional
    public void changePasswordHashed(ChangePasswordHashedRequest changePasswordRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean currentPasswordMatches = false;

        // Verify current password based on storage method
        if ("SHA256_BCRYPT".equals(user.getPasswordHashMethod())) {
            currentPasswordMatches = passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword());
        } else {
            // Legacy BCRYPT user - can't verify SHA256 hash against bcrypt storage
            // For migration, we'll allow them to change password if they provide ANY current password
            // This is a one-time migration path
            currentPasswordMatches = passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword());
        }

        if (!currentPasswordMatches) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Store new password as bcrypt(sha256(newPassword)) and update method
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setPasswordHashMethod("SHA256_BCRYPT");
        user.setFirstTimeLogin(false);
        userRepository.save(user);

        auditLogService.logAction(user, "PASSWORD_CHANGE", "User", user.getId(), "Password changed (hashed)");
    }


    private User buildUserFromSignupRequest(SignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        // For signup, password comes pre-hashed with SHA256 from frontend
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setFirstTimeLogin(true); // All new users must change password on first login
        user.setPasswordHashMethod("SHA256_BCRYPT"); // New users use the secure method
        return user;
    }
}
