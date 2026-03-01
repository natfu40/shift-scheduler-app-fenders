package com.shiftscheduler.controller;

import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.model.AuditLog;
import com.shiftscheduler.model.User;
import com.shiftscheduler.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("AuditLogController Tests")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    private User testUser;
    private AuditLog testAuditLog;
    private List<AuditLog> testAuditLogs;
    private Page<AuditLog> testAuditLogPage;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setUser(testUser);
        testAuditLog.setAction("CREATE_USER");
        testAuditLog.setEntity("User");
        testAuditLog.setEntityId(1L);
        testAuditLog.setDescription("User created successfully");
        testAuditLog.setIpAddress("192.168.1.1");
        testAuditLog.setActionAt(Instant.now());

        testAuditLogs = Arrays.asList(testAuditLog);
        testAuditLogPage = new PageImpl<>(testAuditLogs, PageRequest.of(0, 10), 1);
    }

    @Nested
    @DisplayName("Get All Audit Logs Tests")
    class GetAllAuditLogsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get all audit logs when authenticated as admin")
        void shouldGetAllAuditLogsWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            when(auditLogService.getAllAuditLogs(any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].action").value("CREATE_USER"))
                    .andExpect(jsonPath("$.content[0].entity").value("User"))
                    .andExpect(jsonPath("$.content[0].entityId").value(1L))
                    .andExpect(jsonPath("$.content[0].description").value("User created successfully"))
                    .andExpect(jsonPath("$.content[0].ipAddress").value("192.168.1.1"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));

            verify(auditLogService).getAllAuditLogs(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle pagination parameters")
        void shouldHandlePaginationParameters() throws Exception {
            // Given
            when(auditLogService.getAllAuditLogs(any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("page", "1")
                    .param("size", "20"))
                    .andExpect(status().isOk());

            verify(auditLogService).getAllAuditLogs(eq(PageRequest.of(1, 20)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should use default pagination values")
        void shouldUseDefaultPaginationValues() throws Exception {
            // Given
            when(auditLogService.getAllAuditLogs(any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs"))
                    .andExpect(status().isOk());

            verify(auditLogService).getAllAuditLogs(eq(PageRequest.of(0, 10)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to get all audit logs")
        void shouldReturnForbiddenWhenNonAdminTriesToGetAllAuditLogs() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit-logs"))
                    .andExpect(status().isForbidden());

            verify(auditLogService, never()).getAllAuditLogs(any());
        }

        @Test
        @DisplayName("Should return forbidden when unauthenticated")
        void shouldReturnForbiddenWhenUnauthenticated() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit-logs"))
                    .andExpect(status().isForbidden());

            verify(auditLogService, never()).getAllAuditLogs(any());
        }
    }

    @Nested
    @DisplayName("Get Audit Logs By User Tests")
    class GetAuditLogsByUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get audit logs by user when authenticated as admin")
        void shouldGetAuditLogsByUserWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            when(auditLogService.getAuditLogsByUser(eq(userId), any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].action").value("CREATE_USER"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(auditLogService).getAuditLogsByUser(eq(userId), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle pagination for user audit logs")
        void shouldHandlePaginationForUserAuditLogs() throws Exception {
            // Given
            Long userId = 1L;
            when(auditLogService.getAuditLogsByUser(eq(userId), any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/user/{userId}", userId)
                    .param("page", "2")
                    .param("size", "5"))
                    .andExpect(status().isOk());

            verify(auditLogService).getAuditLogsByUser(eq(userId), eq(PageRequest.of(2, 5)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle empty results for user audit logs")
        void shouldHandleEmptyResultsForUserAuditLogs() throws Exception {
            // Given
            Long userId = 999L;
            Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(auditLogService.getAuditLogsByUser(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(auditLogService).getAuditLogsByUser(eq(userId), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to get user audit logs")
        void shouldReturnForbiddenWhenNonAdminTriesToGetUserAuditLogs() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(get("/api/audit-logs/user/{userId}", userId))
                    .andExpect(status().isForbidden());

            verify(auditLogService, never()).getAuditLogsByUser(any(), any());
        }
    }

    @Nested
    @DisplayName("Get Audit Logs By Action Tests")
    class GetAuditLogsByActionTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get audit logs by action when authenticated as admin")
        void shouldGetAuditLogsByActionWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            String action = "CREATE_USER";
            when(auditLogService.getAuditLogsByAction(eq(action), any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/action/{action}", action))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].action").value("CREATE_USER"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(auditLogService).getAuditLogsByAction(eq(action), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle special characters in action parameter")
        void shouldHandleSpecialCharactersInActionParameter() throws Exception {
            // Given
            String action = "UPDATE_USER_PASSWORD";
            when(auditLogService.getAuditLogsByAction(eq(action), any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/action/{action}", action))
                    .andExpect(status().isOk());

            verify(auditLogService).getAuditLogsByAction(eq(action), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle pagination for action audit logs")
        void shouldHandlePaginationForActionAuditLogs() throws Exception {
            // Given
            String action = "DELETE_USER";
            when(auditLogService.getAuditLogsByAction(eq(action), any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/action/{action}", action)
                    .param("page", "1")
                    .param("size", "25"))
                    .andExpect(status().isOk());

            verify(auditLogService).getAuditLogsByAction(eq(action), eq(PageRequest.of(1, 25)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to get action audit logs")
        void shouldReturnForbiddenWhenNonAdminTriesToGetActionAuditLogs() throws Exception {
            // Given
            String action = "CREATE_USER";

            // When & Then
            mockMvc.perform(get("/api/audit-logs/action/{action}", action))
                    .andExpect(status().isForbidden());

            verify(auditLogService, never()).getAuditLogsByAction(any(), any());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exceptions for get all audit logs")
        void shouldHandleServiceExceptionsForGetAllAuditLogs() throws Exception {
            // Given
            when(auditLogService.getAllAuditLogs(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/api/audit-logs"))
                    .andExpect(status().isInternalServerError());

            verify(auditLogService).getAllAuditLogs(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exceptions for get audit logs by user")
        void shouldHandleServiceExceptionsForGetAuditLogsByUser() throws Exception {
            // Given
            Long userId = 1L;
            when(auditLogService.getAuditLogsByUser(eq(userId), any(Pageable.class)))
                    .thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(get("/api/audit-logs/user/{userId}", userId))
                    .andExpect(status().isInternalServerError());

            verify(auditLogService).getAuditLogsByUser(eq(userId), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle invalid pagination parameters gracefully")
        void shouldHandleInvalidPaginationParametersGracefully() throws Exception {
            // Given - negative page numbers should result in bad request
            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("page", "-1")
                    .param("size", "0"))
                    .andExpect(status().isBadRequest()); // Invalid pagination parameters result in 400

            verify(auditLogService, never()).getAllAuditLogs(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("CORS and HTTP Methods Tests")
    class CorsAndHttpMethodsTests {

        @Test
        @DisplayName("Should handle OPTIONS request for CORS preflight")
        void shouldHandleOptionsRequestForCorsPreflight() throws Exception {
            // When & Then
            mockMvc.perform(options("/api/audit-logs")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle large page sizes")
        void shouldHandleLargePageSizes() throws Exception {
            // Given
            when(auditLogService.getAllAuditLogs(any(Pageable.class))).thenReturn(testAuditLogPage);

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("page", "0")
                    .param("size", "1000"))
                    .andExpect(status().isOk());

            verify(auditLogService).getAllAuditLogs(eq(PageRequest.of(0, 1000)));
        }
    }
}
