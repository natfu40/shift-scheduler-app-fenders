package com.shiftscheduler.service;

import com.shiftscheduler.dto.AuthResponse;
import com.shiftscheduler.dto.ChangePasswordHashedRequest;
import com.shiftscheduler.dto.LoginRequest;
import com.shiftscheduler.dto.SignupRequest;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import com.shiftscheduler.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        signupRequest = new SignupRequest("test@example.com", "sha256HashedPassword", "John", "Doe");
        loginRequest = new LoginRequest("test@example.com", "sha256HashedPassword");
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("bcryptHashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        user.setFirstTimeLogin(false);
        user.setPasswordHashMethod("SHA256_BCRYPT");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Nested
    @DisplayName("Signup Tests")
    class SignupTests {

        @Test
        @DisplayName("Should successfully register new user")
        void shouldSuccessfullyRegisterNewUser() {
            // Given
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("bcryptHashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(signupRequest.getPassword(), testUser.getPassword())).thenReturn(true);
            when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
            when(userRoleRepository.isUserAdmin(testUser.getId())).thenReturn(false);

            // When
            AuthResponse response = authService.signup(signupRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(signupRequest.getEmail());
            assertThat(response.getFirstName()).isEqualTo(signupRequest.getFirstName());
            assertThat(response.getLastName()).isEqualTo(signupRequest.getLastName());
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getType()).isEqualTo("Bearer");

            verify(userRepository).existsByEmail(signupRequest.getEmail());
            verify(userRepository).save(any(User.class));
            verify(auditLogService).logAction(any(User.class), eq("SIGNUP"), eq("User"), any(), contains("User registered"));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should set correct user properties during signup")
        void shouldSetCorrectUserPropertiesDuringSignup() {
            // Given
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("bcryptHashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(signupRequest.getPassword(), testUser.getPassword())).thenReturn(true);
            when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
            when(userRoleRepository.isUserAdmin(testUser.getId())).thenReturn(false);

            // When
            authService.signup(signupRequest);

            // Then
            verify(userRepository).save(argThat(user ->
                user.getEmail().equals(signupRequest.getEmail()) &&
                user.getFirstName().equals(signupRequest.getFirstName()) &&
                user.getLastName().equals(signupRequest.getLastName()) &&
                user.isActive() &&
                user.isFirstTimeLogin() &&
                user.getPasswordHashMethod().equals("SHA256_BCRYPT")
            ));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with correct credentials")
        void shouldSuccessfullyLoginWithCorrectCredentials() {
            // Given
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
            when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
            when(userRoleRepository.isUserAdmin(testUser.getId())).thenReturn(true);

            // When
            AuthResponse response = authService.loginWithHashedPassword(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUserId()).isEqualTo(testUser.getId());
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.isAdmin()).isTrue();
            assertThat(response.isFirstTimeLogin()).isFalse();

            verify(auditLogService).logAction(testUser, "LOGIN", "User", testUser.getId(), "User logged in (hashed)");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.loginWithHashedPassword(loginRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with email: " + loginRequest.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when password hash method is incompatible")
        void shouldThrowExceptionWhenPasswordHashMethodIncompatible() {
            // Given
            testUser.setPasswordHashMethod("BCRYPT");
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.loginWithHashedPassword(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("User account uses incompatible password method. Please contact administrator.");
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowExceptionWhenPasswordIncorrect() {
            // Given
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.loginWithHashedPassword(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        private ChangePasswordHashedRequest changePasswordRequest;

        @BeforeEach
        void setUp() {
            changePasswordRequest = new ChangePasswordHashedRequest();
            changePasswordRequest.setCurrentPassword("currentSha256Hash");
            changePasswordRequest.setNewPassword("newSha256Hash");
        }

        @Test
        @DisplayName("Should successfully change password with SHA256_BCRYPT method")
        void shouldSuccessfullyChangePasswordWithSha256BcryptMethod() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn("newBcryptHashedPassword");

            // When
            authService.changePasswordHashed(changePasswordRequest, testUser.getEmail());

            // Then
            verify(passwordEncoder).encode(changePasswordRequest.getNewPassword());
            verify(userRepository).save(argThat(user ->
                user.getPassword().equals("newBcryptHashedPassword") &&
                user.getPasswordHashMethod().equals("SHA256_BCRYPT") &&
                !user.isFirstTimeLogin()
            ));
            verify(auditLogService).logAction(testUser, "PASSWORD_CHANGE", "User", testUser.getId(), "Password changed (hashed)");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFoundForPasswordChange() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.changePasswordHashed(changePasswordRequest, testUser.getEmail()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with email: " + testUser.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), testUser.getPassword())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.changePasswordHashed(changePasswordRequest, testUser.getEmail()))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Current password is incorrect");
        }

        @Test
        @DisplayName("Should handle legacy BCRYPT user password change")
        void shouldHandleLegacyBcryptUserPasswordChange() {
            // Given
            testUser.setPasswordHashMethod("BCRYPT");
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn("newBcryptHashedPassword");

            // When
            authService.changePasswordHashed(changePasswordRequest, testUser.getEmail());

            // Then
            verify(userRepository).save(argThat(user ->
                user.getPasswordHashMethod().equals("SHA256_BCRYPT")
            ));
        }
    }
}
