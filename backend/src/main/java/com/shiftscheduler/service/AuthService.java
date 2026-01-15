package com.shiftscheduler.service;

import com.shiftscheduler.dto.LoginRequest;
import com.shiftscheduler.dto.SignupRequest;
import com.shiftscheduler.dto.AuthResponse;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.security.JwtTokenProvider;
import com.shiftscheduler.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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
                user.getLastName()
        );
    }

    private User buildUserFromSignupRequest(SignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        return user;
    }
}

