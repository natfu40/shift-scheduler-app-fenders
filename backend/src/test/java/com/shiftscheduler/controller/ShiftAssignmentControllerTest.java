package com.shiftscheduler.controller;

import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.service.ShiftAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShiftAssignmentController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("ShiftAssignmentController Tests")
class ShiftAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftAssignmentService shiftAssignmentService;

    private ShiftAssignmentDTO testAssignmentDTO;
    private List<ShiftAssignmentDTO> testAssignmentList;

    @BeforeEach
    void setUp() {
        testAssignmentDTO = new ShiftAssignmentDTO();
        testAssignmentDTO.setId(1L);
        testAssignmentDTO.setShiftId(1L);
        testAssignmentDTO.setUserId(1L);
        testAssignmentDTO.setShiftName("Morning Shift");
        testAssignmentDTO.setUserEmail("test@example.com");
        testAssignmentDTO.setUserFirstName("John");
        testAssignmentDTO.setUserLastName("Doe");
        testAssignmentDTO.setAccepted(true);
        testAssignmentDTO.setSignedUpAt(Instant.now());
        testAssignmentDTO.setAcceptedAt(Instant.now());

        testAssignmentList = Collections.singletonList(testAssignmentDTO);
    }

    @Nested
    @DisplayName("Signup for Shift Tests")
    class SignupForShiftTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should signup for shift when authenticated")
        void shouldSignupForShiftWhenAuthenticated() throws Exception {
            // Given
            Long shiftId = 1L;
            when(shiftAssignmentService.signupForShift(eq(shiftId), eq(1L))).thenReturn(testAssignmentDTO);

            // When & Then
            mockMvc.perform(post("/api/shift-assignments/signup/{shiftId}", shiftId)
                    .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.shiftId").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.shiftName").value("Morning Shift"));

            verify(shiftAssignmentService).signupForShift(eq(shiftId), eq(1L));
        }

        @Test
        @DisplayName("Should return forbidden when unauthenticated")
        void shouldReturnForbiddenWhenUnauthenticated() throws Exception {
            // Given
            Long shiftId = 1L;

            // When & Then
            mockMvc.perform(post("/api/shift-assignments/signup/{shiftId}", shiftId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(shiftAssignmentService, never()).signupForShift(any(), any());
        }
    }

    @Nested
    @DisplayName("Accept Signup Tests")
    class AcceptSignupTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should accept signup when authenticated as admin")
        void shouldAcceptSignupWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long assignmentId = 1L;
            when(shiftAssignmentService.acceptSignup(eq(assignmentId), eq(1L))).thenReturn(testAssignmentDTO);

            // When & Then
            mockMvc.perform(put("/api/shift-assignments/{assignmentId}/accept", assignmentId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.accepted").value(true));

            verify(shiftAssignmentService).acceptSignup(eq(assignmentId), eq(1L));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to accept signup")
        void shouldReturnForbiddenWhenNonAdminTriesToAcceptSignup() throws Exception {
            // Given
            Long assignmentId = 1L;

            // When & Then
            mockMvc.perform(put("/api/shift-assignments/{assignmentId}/accept", assignmentId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(shiftAssignmentService, never()).acceptSignup(any(), any());
        }
    }

    @Nested
    @DisplayName("Reject Signup Tests")
    class RejectSignupTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should reject signup when authenticated as admin")
        void shouldRejectSignupWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long assignmentId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/shift-assignments/{assignmentId}/reject", assignmentId)
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(shiftAssignmentService).rejectSignup(eq(assignmentId), eq(1L));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to reject signup")
        void shouldReturnForbiddenWhenNonAdminTriesToRejectSignup() throws Exception {
            // Given
            Long assignmentId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/shift-assignments/{assignmentId}/reject", assignmentId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(shiftAssignmentService, never()).rejectSignup(any(), any());
        }
    }

    @Nested
    @DisplayName("Get Signups for Shift Tests")
    class GetSignupsForShiftTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get signups for shift when authenticated as admin")
        void shouldGetSignupsForShiftWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long shiftId = 1L;
            when(shiftAssignmentService.getSignupsForShift(shiftId)).thenReturn(testAssignmentList);

            // When & Then
            mockMvc.perform(get("/api/shift-assignments/shift/{shiftId}", shiftId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].shiftId").value(1L))
                    .andExpect(jsonPath("$[0].userId").value(1L));

            verify(shiftAssignmentService).getSignupsForShift(shiftId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return empty list when no signups found")
        void shouldReturnEmptyListWhenNoSignupsFound() throws Exception {
            // Given
            Long shiftId = 999L;
            when(shiftAssignmentService.getSignupsForShift(shiftId)).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/shift-assignments/shift/{shiftId}", shiftId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(shiftAssignmentService).getSignupsForShift(shiftId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to get signups for shift")
        void shouldReturnForbiddenWhenNonAdminTriesToGetSignupsForShift() throws Exception {
            // Given
            Long shiftId = 1L;

            // When & Then
            mockMvc.perform(get("/api/shift-assignments/shift/{shiftId}", shiftId))
                    .andExpect(status().isForbidden());

            verify(shiftAssignmentService, never()).getSignupsForShift(any());
        }
    }

    @Nested
    @DisplayName("Get User Shift Assignments Tests")
    class GetUserShiftAssignmentsTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should get user's shift assignments when authenticated")
        void shouldGetUsersShiftAssignmentsWhenAuthenticated() throws Exception {
            // Given
            when(shiftAssignmentService.getUserShiftAssignments(1L)).thenReturn(testAssignmentList);

            // When & Then
            mockMvc.perform(get("/api/shift-assignments/user"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].userId").value(1L))
                    .andExpect(jsonPath("$[0].userEmail").value("test@example.com"));

            verify(shiftAssignmentService).getUserShiftAssignments(1L);
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should return empty list when user has no assignments")
        void shouldReturnEmptyListWhenUserHasNoAssignments() throws Exception {
            // Given
            when(shiftAssignmentService.getUserShiftAssignments(1L)).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/shift-assignments/user"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(shiftAssignmentService).getUserShiftAssignments(1L);
        }

        @Test
        @DisplayName("Should return forbidden when unauthenticated")
        void shouldReturnForbiddenWhenUnauthenticatedForUserAssignments() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/shift-assignments/user"))
                    .andExpect(status().isForbidden());

            verify(shiftAssignmentService, never()).getUserShiftAssignments(any());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should handle service exceptions for signup")
        void shouldHandleServiceExceptionsForSignup() throws Exception {
            // Given
            Long shiftId = 999L;
            when(shiftAssignmentService.signupForShift(eq(shiftId), eq(1L)))
                    .thenThrow(new RuntimeException("Shift not found"));

            // When & Then
            mockMvc.perform(post("/api/shift-assignments/signup/{shiftId}", shiftId)
                    .with(csrf()))
                    .andExpect(status().isInternalServerError());

            verify(shiftAssignmentService).signupForShift(eq(shiftId), eq(1L));
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should handle service exceptions for accept signup")
        void shouldHandleServiceExceptionsForAcceptSignup() throws Exception {
            // Given
            Long assignmentId = 999L;
            when(shiftAssignmentService.acceptSignup(eq(assignmentId), eq(1L)))
                    .thenThrow(new RuntimeException("Assignment not found"));

            // When & Then
            mockMvc.perform(put("/api/shift-assignments/{assignmentId}/accept", assignmentId)
                    .with(csrf()))
                    .andExpect(status().isInternalServerError());

            verify(shiftAssignmentService).acceptSignup(eq(assignmentId), eq(1L));
        }
    }

    @Nested
    @DisplayName("CORS and HTTP Methods Tests")
    class CorsAndHttpMethodsTests {

        @Test
        @DisplayName("Should handle OPTIONS request for CORS preflight")
        void shouldHandleOptionsRequestForCorsPreflight() throws Exception {
            // When & Then
            mockMvc.perform(options("/api/shift-assignments/signup/1")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk());
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should handle path variables correctly")
        void shouldHandlePathVariablesCorrectly() throws Exception {
            // Given
            Long shiftId = 123L;
            when(shiftAssignmentService.getSignupsForShift(shiftId)).thenReturn(testAssignmentList);

            // When & Then
            mockMvc.perform(get("/api/shift-assignments/shift/{shiftId}", shiftId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));

            verify(shiftAssignmentService).getSignupsForShift(shiftId);
        }
    }
}
