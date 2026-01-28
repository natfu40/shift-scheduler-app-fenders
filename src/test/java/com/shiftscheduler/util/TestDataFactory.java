package com.shiftscheduler.util;

import com.shiftscheduler.dto.LoginRequest;
import com.shiftscheduler.dto.SignupRequest;
import com.shiftscheduler.model.User;

import java.time.LocalDateTime;

/**
 * Factory class for creating test data objects consistently across test classes.
 * This helps maintain consistency and reduces code duplication in tests.
 */
public class TestDataFactory {

    private TestDataFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a basic test user with default values
     */
    public static User createTestUser() {
        return createTestUser(1L, "test@example.com", "John", "Doe");
    }

    /**
     * Creates a test user with specified parameters
     */
    public static User createTestUser(Long id, String email, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        user.setFirstTimeLogin(true);
        user.setPasswordHashMethod("SHA256_BCRYPT");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Creates an admin test user
     */
    public static User createAdminUser() {
        return createTestUser(999L, "admin@example.com", "Admin", "User");
    }

    /**
     * Creates a user with first time login set to false
     */
    public static User createExistingUser() {
        User user = createTestUser();
        user.setFirstTimeLogin(false);
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Creates a test signup request
     */
    public static SignupRequest createSignupRequest() {
        return new SignupRequest("test@example.com", "sha256HashedPassword", "John", "Doe");
    }

    /**
     * Creates a signup request with specified email
     */
    public static SignupRequest createSignupRequest(String email) {
        return new SignupRequest(email, "sha256HashedPassword", "John", "Doe");
    }

    /**
     * Creates a signup request with all parameters
     */
    public static SignupRequest createSignupRequest(String email, String password, String firstName, String lastName) {
        return new SignupRequest(email, password, firstName, lastName);
    }

    /**
     * Creates a test login request
     */
    public static LoginRequest createLoginRequest() {
        return new LoginRequest("test@example.com", "sha256HashedPassword");
    }

    /**
     * Creates a login request with specified credentials
     */
    public static LoginRequest createLoginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    /**
     * Creates a user with legacy password hash method for migration testing
     */
    public static User createLegacyBcryptUser() {
        User user = createTestUser();
        user.setPasswordHashMethod("BCRYPT");
        user.setPassword("bcryptOnlyHashedPassword");
        return user;
    }

    /**
     * Creates an inactive user for testing access restrictions
     */
    public static User createInactiveUser() {
        User user = createTestUser();
        user.setActive(false);
        return user;
    }

    /**
     * Creates a user with updated timestamp for update testing
     */
    public static User createUpdatedUser() {
        User user = createTestUser();
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return user;
    }
}
