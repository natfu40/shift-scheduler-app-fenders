package com.shiftscheduler.controller;

import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.security.CustomUserDetailsService;
import com.shiftscheduler.security.JwtTokenProvider;
import com.shiftscheduler.security.UserPrincipal;
import com.shiftscheduler.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("CalendarController Tests")
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UserPrincipal testUserPrincipal;
    private String testICSContent;

    @BeforeEach
    void setUp() {
        testUserPrincipal = new UserPrincipal(1L, "test@example.com", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

        testICSContent = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n";
    }

    @Nested
    @DisplayName("Get User Shifts ICS Tests")
    class GetUserShiftsICSTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should allow user to access their own calendar")
        void shouldAllowUserToAccessTheirOwnCalendar() throws Exception {
            // Given
            Long userId = 1L;
            when(calendarService.generateUserShiftsICS(userId)).thenReturn(testICSContent);

            // When & Then
            mockMvc.perform(get("/api/calendar/user/{userId}/shifts.ics", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/calendar"))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=my-shifts.ics"))
                    .andExpect(content().string(testICSContent));

            verify(calendarService).generateUserShiftsICS(userId);
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should allow admin to access any user's calendar")
        void shouldAllowAdminToAccessAnyUsersCalendar() throws Exception {
            // Given
            Long userId = 2L;
            when(calendarService.generateUserShiftsICS(userId)).thenReturn(testICSContent);

            // When & Then
            mockMvc.perform(get("/api/calendar/user/{userId}/shifts.ics", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/calendar"));

            verify(calendarService).generateUserShiftsICS(userId);
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should return forbidden when user tries to access another user's calendar")
        void shouldReturnForbiddenWhenUserTriesToAccessAnotherUsersCalendar() throws Exception {
            // Given
            Long otherUserId = 2L;

            // When & Then
            mockMvc.perform(get("/api/calendar/user/{userId}/shifts.ics", otherUserId))
                    .andExpect(status().isForbidden());

            verify(calendarService, never()).generateUserShiftsICS(any());
        }
    }

    @Nested
    @DisplayName("Get My Shifts ICS Tests")
    class GetMyShiftsICSTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should get authenticated user's calendar")
        void shouldGetAuthenticatedUsersCalendar() throws Exception {
            // Given
            when(calendarService.generateUserShiftsICS(1L)).thenReturn(testICSContent);

            // When & Then
            mockMvc.perform(get("/api/calendar/user/my-shifts.ics"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/calendar"))
                    .andExpect(header().string("Content-Disposition", "inline; filename=my-shifts.ics"));

            verify(calendarService).generateUserShiftsICS(1L);
        }

        @Test
        @DisplayName("Should authenticate with valid JWT token parameter")
        void shouldAuthenticateWithValidJwtTokenParameter() throws Exception {
            // Given
            String validToken = "valid.jwt.token";
            Long userId = 1L;

            when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
            when(userDetailsService.loadUserById(userId)).thenReturn(testUserPrincipal);
            when(calendarService.generateUserShiftsICS(userId)).thenReturn(testICSContent);

            // When & Then - The endpoint works with valid token
            mockMvc.perform(get("/api/calendar/user/my-shifts.ics")
                            .param("token", validToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/calendar"));

            verify(calendarService).generateUserShiftsICS(userId);
        }

        @Test
        @DisplayName("Should return unauthorized with invalid JWT token")
        void shouldReturnUnauthorizedWithInvalidJwtToken() throws Exception {
            // Given
            String invalidToken = "invalid.jwt.token";
            when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

            // When & Then - Invalid token returns 401 Unauthorized
            mockMvc.perform(get("/api/calendar/user/my-shifts.ics")
                            .param("token", invalidToken))
                    .andExpect(status().isUnauthorized()); // Actual behavior is 401

            verify(calendarService, never()).generateUserShiftsICS(any());
        }
    }
}
