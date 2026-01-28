package com.shiftscheduler.controller;

import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.dto.AdminResponse;
import com.shiftscheduler.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    private AdminResponse testAdminResponse;

    @BeforeEach
    void setUp() {
        testAdminResponse = new AdminResponse();
        testAdminResponse.setUserId(1L);
        testAdminResponse.setEmail("test@example.com");
        testAdminResponse.setMessage("Operation successful");
        testAdminResponse.setSuccess(true);
    }

    @Nested
    @DisplayName("Make User Admin Tests")
    class MakeUserAdminTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should make user admin when authenticated as admin")
        void shouldMakeUserAdminWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            when(adminService.assignAdminRole(userId)).thenReturn(testAdminResponse);

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/make-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.message").value("Operation successful"))
                    .andExpect(jsonPath("$.success").value(true));

            verify(adminService).assignAdminRole(userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to make user admin")
        void shouldReturnForbiddenWhenNonAdminTriesToMakeUserAdmin() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/make-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(adminService, never()).assignAdminRole(any());
        }

        @Test
        @DisplayName("Should return forbidden when unauthenticated")
        void shouldReturnForbiddenWhenUnauthenticated() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/make-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(adminService, never()).assignAdminRole(any());
        }
    }

    @Nested
    @DisplayName("Remove Admin From User Tests")
    class RemoveAdminFromUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should remove admin role when authenticated as admin")
        void shouldRemoveAdminRoleWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            AdminResponse removeResponse = new AdminResponse();
            removeResponse.setUserId(1L);
            removeResponse.setEmail("test@example.com");
            removeResponse.setMessage("Admin role removed");
            removeResponse.setSuccess(true);

            when(adminService.removeAdminRole(userId)).thenReturn(removeResponse);

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/remove-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.message").value("Admin role removed"))
                    .andExpect(jsonPath("$.success").value(true));

            verify(adminService).removeAdminRole(userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to remove admin role")
        void shouldReturnForbiddenWhenNonAdminTriesToRemoveAdminRole() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/remove-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(adminService, never()).removeAdminRole(any());
        }

        @Test
        @DisplayName("Should return forbidden when unauthenticated for remove admin")
        void shouldReturnForbiddenWhenUnauthenticatedForRemoveAdmin() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/remove-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(adminService, never()).removeAdminRole(any());
        }
    }

    @Nested
    @DisplayName("Is User Admin Tests")
    class IsUserAdminTests {

        @Test
        @DisplayName("Should check if user is admin - returns true")
        void shouldCheckIfUserIsAdminReturnsTrue() throws Exception {
            // Given
            Long userId = 1L;
            when(adminService.isUserAdmin(userId)).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/admin/users/{userId}/is-admin", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(content().string("true"));

            verify(adminService).isUserAdmin(userId);
        }

        @Test
        @DisplayName("Should check if user is admin - returns false")
        void shouldCheckIfUserIsAdminReturnsFalse() throws Exception {
            // Given
            Long userId = 1L;
            when(adminService.isUserAdmin(userId)).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/admin/users/{userId}/is-admin", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(content().string("false"));

            verify(adminService).isUserAdmin(userId);
        }

        @Test
        @DisplayName("Should allow unauthenticated access to is-admin endpoint")
        void shouldAllowUnauthenticatedAccessToIsAdminEndpoint() throws Exception {
            // Given
            Long userId = 1L;
            when(adminService.isUserAdmin(userId)).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/admin/users/{userId}/is-admin", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));

            verify(adminService).isUserAdmin(userId);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exceptions for make admin")
        void shouldHandleServiceExceptionsForMakeAdmin() throws Exception {
            // Given
            Long userId = 999L;
            when(adminService.assignAdminRole(userId)).thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/make-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isInternalServerError());

            verify(adminService).assignAdminRole(userId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exceptions for remove admin")
        void shouldHandleServiceExceptionsForRemoveAdmin() throws Exception {
            // Given
            Long userId = 999L;
            when(adminService.removeAdminRole(userId)).thenThrow(new RuntimeException("User does not have ADMIN role"));

            // When & Then
            mockMvc.perform(post("/api/admin/users/{userId}/remove-admin", userId)
                    .with(csrf()))
                    .andExpect(status().isInternalServerError());

            verify(adminService).removeAdminRole(userId);
        }

        @Test
        @DisplayName("Should handle service exceptions for is admin check")
        void shouldHandleServiceExceptionsForIsAdminCheck() throws Exception {
            // Given
            Long userId = 999L;
            when(adminService.isUserAdmin(userId)).thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/api/admin/users/{userId}/is-admin", userId))
                    .andExpect(status().isInternalServerError());

            verify(adminService).isUserAdmin(userId);
        }
    }

    @Nested
    @DisplayName("CORS and HTTP Methods Tests")
    class CorsAndHttpMethodsTests {

        @Test
        @DisplayName("Should handle OPTIONS request for CORS preflight")
        void shouldHandleOptionsRequestForCorsPreflight() throws Exception {
            // When & Then
            mockMvc.perform(options("/api/admin/users/1/make-admin")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle path variables correctly")
        void shouldHandlePathVariablesCorrectly() throws Exception {
            // Given
            Long userId = 123L;
            when(adminService.isUserAdmin(userId)).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/admin/users/{userId}/is-admin", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(adminService).isUserAdmin(userId);
        }
    }
}
