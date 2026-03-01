package com.shiftscheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.TestSecurityConfig;
import com.shiftscheduler.dto.AuthResponse;
import com.shiftscheduler.dto.ChangePasswordHashedRequest;
import com.shiftscheduler.dto.LoginRequest;
import com.shiftscheduler.dto.SignupRequest;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({TestControllerConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private ChangePasswordHashedRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest("test@example.com", "sha256HashedPassword", "John", "Doe");
        loginRequest = new LoginRequest("test@example.com", "sha256HashedPassword");
        authResponse = new AuthResponse("jwt-token", "Bearer", 1L, "test@example.com",
                "John", "Doe", false, false);

        changePasswordRequest = new ChangePasswordHashedRequest();
        changePasswordRequest.setCurrentPassword("currentSha256Hash");
        changePasswordRequest.setNewPassword("newSha256Hash");
    }

    @Nested
    @DisplayName("Signup Endpoint Tests")
    class SignupTests {

        @Test
        @DisplayName("Should successfully register new user")
        void shouldSuccessfullyRegisterNewUser() throws Exception {
            // Given
            when(authService.signup(any(SignupRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.firstTimeLogin").value(false))
                    .andExpect(jsonPath("$.admin").value(false));

            verify(authService).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for invalid signup data")
        void shouldReturnBadRequestForInvalidSignupData() throws Exception {
            // Given
            SignupRequest invalidRequest = new SignupRequest();
            when(authService.signup(any(SignupRequest.class)))
                    .thenThrow(new IllegalArgumentException("Email already exists"));

            // When & Then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle email already exists error")
        void shouldHandleEmailAlreadyExistsError() throws Exception {
            // Given
            when(authService.signup(any(SignupRequest.class)))
                    .thenThrow(new IllegalArgumentException("Email already exists"));

            // When & Then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void shouldSuccessfullyLoginWithValidCredentials() throws Exception {
            // Given
            when(authService.loginWithHashedPassword(any(LoginRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.email").value("test@example.com"));

            verify(authService).loginWithHashedPassword(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should successfully login via login-hashed endpoint")
        void shouldSuccessfullyLoginViaLoginHashedEndpoint() throws Exception {
            // Given
            when(authService.loginWithHashedPassword(any(LoginRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login-hashed")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).loginWithHashedPassword(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            // Given
            when(authService.loginWithHashedPassword(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return not found when user doesn't exist")
        void shouldReturnNotFoundWhenUserDoesntExist() throws Exception {
            // Given
            when(authService.loginWithHashedPassword(any(LoginRequest.class)))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Change Password Endpoint Tests")
    class ChangePasswordTests {

        @Test
        @WithMockUser(username = "test@example.com")
        @DisplayName("Should successfully change password for authenticated user")
        void shouldSuccessfullyChangePasswordForAuthenticatedUser() throws Exception {
            // Given
            doNothing().when(authService).changePasswordHashed(any(ChangePasswordHashedRequest.class), eq("test@example.com"));

            // When & Then
            mockMvc.perform(post("/api/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changePasswordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Password changed successfully"));

            verify(authService).changePasswordHashed(any(ChangePasswordHashedRequest.class), eq("test@example.com"));
        }

        @Test
        @WithMockUser(username = "test@example.com")
        @DisplayName("Should successfully change password via change-password-hashed endpoint")
        void shouldSuccessfullyChangePasswordViaHashedEndpoint() throws Exception {
            // Given
            doNothing().when(authService).changePasswordHashed(any(ChangePasswordHashedRequest.class), eq("test@example.com"));

            // When & Then
            mockMvc.perform(post("/api/auth/change-password-hashed")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changePasswordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Password changed successfully"));
        }

        @Test
        @DisplayName("Should require authentication for change password")
        void shouldRequireAuthenticationForChangePassword() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changePasswordRequest)))
                    .andExpect(status().isForbidden()); // Spring Security returns 403 for unauthenticated requests

            verify(authService, never()).changePasswordHashed(any(), any());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        @DisplayName("Should return bad request for incorrect current password")
        void shouldReturnBadRequestForIncorrectCurrentPassword() throws Exception {
            // Given
            doThrow(new BadCredentialsException("Current password is incorrect"))
                    .when(authService).changePasswordHashed(any(ChangePasswordHashedRequest.class), eq("test@example.com"));

            // When & Then
            mockMvc.perform(post("/api/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changePasswordRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        @DisplayName("Should return not found when user doesn't exist for password change")
        void shouldReturnNotFoundWhenUserDoesntExistForPasswordChange() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("User not found"))
                    .when(authService).changePasswordHashed(any(ChangePasswordHashedRequest.class), eq("test@example.com"));

            // When & Then
            mockMvc.perform(post("/api/auth/change-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changePasswordRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("CORS and Security Tests")
    class CorsAndSecurityTests {

        @Test
        @DisplayName("Should allow CORS requests")
        void shouldAllowCorsRequests() throws Exception {
            // Given
            when(authService.signup(any(SignupRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .header("Origin", "http://localhost:3000")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"));
        }
    }
}
