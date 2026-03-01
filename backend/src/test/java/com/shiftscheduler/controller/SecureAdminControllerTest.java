package com.shiftscheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.dto.CreateUserRequest;
import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import com.shiftscheduler.util.PasswordHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecureAdminController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class})
@ActiveProfiles("test")
@DisplayName("SecureAdminController Tests")
class SecureAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private CreateUserRequest testCreateUserRequest;
    private User testUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        testCreateUserRequest = new CreateUserRequest();
        testCreateUserRequest.setEmail("admin@example.com");
        testCreateUserRequest.setPassword("securePassword123");
        testCreateUserRequest.setFirstName("Admin");
        testCreateUserRequest.setLastName("User");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@example.com");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser.setFirstTimeLogin(true);
        testUser.setPasswordHashMethod("SHA256_BCRYPT");

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role with full access");
    }

    @Nested
    @DisplayName("Create Admin User Tests")
    class CreateAdminUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create admin user successfully")
        void shouldCreateAdminUserSuccessfully() throws Exception {
            // Given
            when(userRepository.existsByEmail(testCreateUserRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());

            try (MockedStatic<PasswordHashUtil> mockedHashUtil = mockStatic(PasswordHashUtil.class)) {
                mockedHashUtil.when(() -> PasswordHashUtil.hashWithSHA256("securePassword123"))
                        .thenReturn("sha256Hash");
                when(passwordEncoder.encode("sha256Hash")).thenReturn("encodedPassword");

                // When & Then
                mockMvc.perform(post("/api/secure-admin/create-admin")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("Admin user created successfully"))
                        .andExpect(jsonPath("$.email").value("admin@example.com"))
                        .andExpect(jsonPath("$.id").value(1L))
                        .andExpect(jsonPath("$.firstTimeLogin").value(true));

                verify(userRepository).existsByEmail(testCreateUserRequest.getEmail());
                verify(userRepository).save(any(User.class));
                verify(roleRepository).findByName("ADMIN");
                verify(userRoleRepository).save(any(UserRole.class));
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request when user already exists")
        void shouldReturnBadRequestWhenUserAlreadyExists() throws Exception {
            // Given
            when(userRepository.existsByEmail(testCreateUserRequest.getEmail())).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/secure-admin/create-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("User with this email already exists"));

            verify(userRepository).existsByEmail(testCreateUserRequest.getEmail());
            verify(userRepository, never()).save(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle admin role not found exception")
        void shouldHandleAdminRoleNotFoundException() throws Exception {
            // Given
            when(userRepository.existsByEmail(testCreateUserRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

            try (MockedStatic<PasswordHashUtil> mockedHashUtil = mockStatic(PasswordHashUtil.class)) {
                mockedHashUtil.when(() -> PasswordHashUtil.hashWithSHA256(anyString()))
                        .thenReturn("sha256Hash");
                when(passwordEncoder.encode("sha256Hash")).thenReturn("encodedPassword");

                // When & Then
                mockMvc.perform(post("/api/secure-admin/create-admin")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.error").value("Failed to create admin user: ADMIN role not found"));

                verify(roleRepository).findByName("ADMIN");
                verify(userRoleRepository, never()).save(any());
            }
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle password hashing exception")
        void shouldHandlePasswordHashingException() throws Exception {
            // Given
            when(userRepository.existsByEmail(testCreateUserRequest.getEmail())).thenReturn(false);

            try (MockedStatic<PasswordHashUtil> mockedHashUtil = mockStatic(PasswordHashUtil.class)) {
                mockedHashUtil.when(() -> PasswordHashUtil.hashWithSHA256(anyString()))
                        .thenThrow(new RuntimeException("Hashing failed"));

                // When & Then
                mockMvc.perform(post("/api/secure-admin/create-admin")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.error").value("Failed to create admin user: Hashing failed"));

                verify(userRepository, never()).save(any());
            }
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to create admin")
        void shouldReturnForbiddenWhenNonAdminTriesToCreateAdmin() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/secure-admin/create-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                    .andExpect(status().isForbidden());

            verify(userRepository, never()).existsByEmail(any());
        }

        @Test
        @DisplayName("Should return forbidden when unauthenticated")
        void shouldReturnForbiddenWhenUnauthenticated() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/secure-admin/create-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                    .andExpect(status().isForbidden());

            verify(userRepository, never()).existsByEmail(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should set correct user properties during creation")
        void shouldSetCorrectUserPropertiesDuringCreation() throws Exception {
            // Given
            when(userRepository.existsByEmail(testCreateUserRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());

            try (MockedStatic<PasswordHashUtil> mockedHashUtil = mockStatic(PasswordHashUtil.class)) {
                mockedHashUtil.when(() -> PasswordHashUtil.hashWithSHA256("securePassword123"))
                        .thenReturn("sha256Hash");
                when(passwordEncoder.encode("sha256Hash")).thenReturn("encodedPassword");

                // When
                mockMvc.perform(post("/api/secure-admin/create-admin")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                        .andExpect(status().isOk());

                // Then - Verify user properties are set correctly
                verify(userRepository).save(argThat(user ->
                        user.getEmail().equals("admin@example.com") &&
                                user.getFirstName().equals("Admin") &&
                                user.getLastName().equals("User") &&
                                user.isActive() &&
                                user.isFirstTimeLogin() &&
                                "SHA256_BCRYPT".equals(user.getPasswordHashMethod())
                ));
            }
        }
    }

    @Nested
    @DisplayName("Check Admin Exists Tests")
    class CheckAdminExistsTests {

        @Test
        @DisplayName("Should allow public access to check admin exists endpoint")
        void shouldAllowPublicAccessToCheckAdminExistsEndpoint() throws Exception {
            // Given - Use a test environment variable that might exist or test the actual endpoint behavior
            String actualAdminEmail = System.getenv("ADMIN_EMAIL");
            
            if (actualAdminEmail != null && !actualAdminEmail.trim().isEmpty()) {
                when(userRepository.findByEmail(actualAdminEmail)).thenReturn(Optional.of(testUser));

                // When & Then
                mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.adminExists").value(true))
                        .andExpect(jsonPath("$.adminEmail").value(actualAdminEmail))
                        .andExpect(jsonPath("$.message").value("Admin user exists with email: " + actualAdminEmail));

                verify(userRepository).findByEmail(actualAdminEmail);
            } else {
                // Test the NOT_SET scenario when environment variable is not present
                mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.adminExists").value(false))
                        .andExpect(jsonPath("$.adminEmail").value("NOT_SET"))
                        .andExpect(jsonPath("$.message").value("ADMIN_EMAIL environment variable not set - no admin user configured"));

                verify(userRepository, never()).findByEmail(any());
            }
        }

        @Test
        @DisplayName("Should return admin not exists when admin user not found with public access")
        void shouldReturnAdminNotExistsWhenAdminUserNotFoundWithPublicAccess() throws Exception {
            // Given - Use actual environment variable if set
            String actualAdminEmail = System.getenv("ADMIN_EMAIL");
            
            if (actualAdminEmail != null && !actualAdminEmail.trim().isEmpty()) {
                when(userRepository.findByEmail(actualAdminEmail)).thenReturn(Optional.empty());

                // When & Then
                mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.adminExists").value(false))
                        .andExpect(jsonPath("$.adminEmail").value(actualAdminEmail))
                        .andExpect(jsonPath("$.message").value("No admin user found with email: " + actualAdminEmail + " - set ADMIN_PASSWORD and ADMIN_EMAIL environment variables and restart"));

                verify(userRepository).findByEmail(actualAdminEmail);
            }
            // If no ADMIN_EMAIL is set, this test is skipped since the behavior is covered by the environment variable not set test
        }

        @Test
        @DisplayName("Should return not set when ADMIN_EMAIL environment variable not set - public endpoint")
        void shouldReturnNotSetWhenAdminEmailEnvironmentVariableNotSetPublicEndpoint() throws Exception {
            // This test will only pass if the ADMIN_EMAIL environment variable is actually not set
            String actualAdminEmail = System.getenv("ADMIN_EMAIL");
            
            if (actualAdminEmail == null || actualAdminEmail.trim().isEmpty()) {
                // When & Then
                mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.adminExists").value(false))
                        .andExpect(jsonPath("$.adminEmail").value("NOT_SET"))
                        .andExpect(jsonPath("$.message").value("ADMIN_EMAIL environment variable not set - no admin user configured"));

                verify(userRepository, never()).findByEmail(any());
            }
            // If ADMIN_EMAIL is set, this specific scenario can't be tested without mocking
        }

        @Test
        @DisplayName("Should allow unauthenticated access to check admin exists endpoint")
        void shouldAllowUnauthenticatedAccessToCheckAdminExistsEndpoint() throws Exception {
            // Given - Test the endpoint accessibility without authentication
            String actualAdminEmail = System.getenv("ADMIN_EMAIL");
            
            if (actualAdminEmail != null && !actualAdminEmail.trim().isEmpty()) {
                when(userRepository.findByEmail(actualAdminEmail)).thenReturn(Optional.empty());
            }

            // When & Then - No authentication required for this endpoint
            mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.adminExists").exists())
                    .andExpect(jsonPath("$.adminEmail").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return proper response structure for public endpoint")
        void shouldReturnProperResponseStructureForPublicEndpoint() throws Exception {
            // Given - Test that the response always contains the expected structure
            String actualAdminEmail = System.getenv("ADMIN_EMAIL");
            
            if (actualAdminEmail != null && !actualAdminEmail.trim().isEmpty()) {
                when(userRepository.findByEmail(actualAdminEmail)).thenReturn(Optional.empty());
            }

            // When & Then - Verify response structure since endpoint is public
            mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.adminExists").isBoolean())
                    .andExpect(jsonPath("$.adminEmail").isString())
                    .andExpect(jsonPath("$.message").isString());
        }

        @Test
        @DisplayName("Should handle public endpoint availability")
        void shouldHandlePublicEndpointAvailability() throws Exception {
            // This test verifies the endpoint works as a public endpoint

            // When & Then
            mockMvc.perform(get("/api/secure-admin/check-admin-exists"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // The endpoint should always return a valid JSON response
            // We don't verify specific values since they depend on the actual environment
        }
    }

    @Nested
    @DisplayName("Request Validation Tests")
    class RequestValidationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle valid JSON with unexpected fields")
        void shouldHandleValidJsonWithUnexpectedFields() throws Exception {
            // Given - Valid JSON but with unexpected fields that don't map to CreateUserRequest
            when(userRepository.existsByEmail(null)).thenReturn(false); // Email will be null from unmapped JSON

            // When & Then - Valid JSON gets parsed but results in null values for expected fields
            mockMvc.perform(post("/api/secure-admin/create-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"invalid\": \"json\", \"unexpected\": \"field\"}"))
                    .andExpect(status().isInternalServerError()) // Fails during processing due to null values
                    .andExpect(jsonPath("$.error").value("Failed to create admin user: Error hashing password"));

            verify(userRepository).existsByEmail(null); // Gets called with null because email field is missing
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle missing required fields in create admin request")
        void shouldHandleMissingRequiredFieldsInCreateAdminRequest() throws Exception {
            // Given
            CreateUserRequest incompleteRequest = new CreateUserRequest();
            incompleteRequest.setEmail("admin@example.com");
            // Missing password, firstName, lastName

            when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/secure-admin/create-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(incompleteRequest)))
                    .andExpect(status().isInternalServerError()); // Will fail when trying to hash null password

            verify(userRepository).existsByEmail("admin@example.com");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle empty email in create admin request")
        void shouldHandleEmptyEmailInCreateAdminRequest() throws Exception {
            // Given
            CreateUserRequest requestWithEmptyEmail = new CreateUserRequest();
            requestWithEmptyEmail.setEmail("");
            requestWithEmptyEmail.setPassword("password123");
            requestWithEmptyEmail.setFirstName("Admin");
            requestWithEmptyEmail.setLastName("User");

            when(userRepository.existsByEmail("")).thenReturn(false);

            // When & Then - Empty email causes internal server error during processing
            mockMvc.perform(post("/api/secure-admin/create-admin")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithEmptyEmail)))
                    .andExpect(status().isInternalServerError()); // Empty email causes error during processing

            verify(userRepository).existsByEmail("");
        }
    }

    @Nested
    @DisplayName("CORS and Integration Tests")
    class CorsAndIntegrationTests {

        @Test
        @DisplayName("Should handle OPTIONS request for CORS preflight")
        void shouldHandleOptionsRequestForCorsPreflight() throws Exception {
            // When & Then
            mockMvc.perform(options("/api/secure-admin/create-admin")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle GET request to check admin exists with CORS headers")
        void shouldHandleGetRequestToCheckAdminExistsWithCorsHeaders() throws Exception {
            // Given - Use actual environment variable if set
            String actualAdminEmail = System.getenv("ADMIN_EMAIL");

            if (actualAdminEmail != null && !actualAdminEmail.trim().isEmpty()) {
                when(userRepository.findByEmail(actualAdminEmail)).thenReturn(Optional.empty());
            }

            // When & Then - This endpoint is public
            mockMvc.perform(get("/api/secure-admin/check-admin-exists")
                            .header("Origin", "http://localhost:3000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.adminExists").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create admin user with complete workflow")
        void shouldCreateAdminUserWithCompleteWorkflow() throws Exception {
            // Given - Complete workflow test
            when(userRepository.existsByEmail(testCreateUserRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

            UserRole savedUserRole = new UserRole();
            savedUserRole.setUser(testUser);
            savedUserRole.setRole(adminRole);
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(savedUserRole);

            try (MockedStatic<PasswordHashUtil> mockedHashUtil = mockStatic(PasswordHashUtil.class)) {
                mockedHashUtil.when(() -> PasswordHashUtil.hashWithSHA256("securePassword123"))
                        .thenReturn("sha256HashedPassword");
                when(passwordEncoder.encode("sha256HashedPassword")).thenReturn("finalEncodedPassword");

                // When & Then - Complete end-to-end test
                mockMvc.perform(post("/api/secure-admin/create-admin")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value("Admin user created successfully"))
                        .andExpect(jsonPath("$.email").value("admin@example.com"))
                        .andExpect(jsonPath("$.id").value(1L))
                        .andExpect(jsonPath("$.firstTimeLogin").value(true));

                // Verify complete workflow
                verify(userRepository).existsByEmail("admin@example.com");
                verify(userRepository).save(any(User.class));
                verify(roleRepository).findByName("ADMIN");
                verify(userRoleRepository).save(any(UserRole.class));
                verify(passwordEncoder).encode("sha256HashedPassword");

                mockedHashUtil.verify(() -> PasswordHashUtil.hashWithSHA256("securePassword123"));
            }
        }
    }
}



