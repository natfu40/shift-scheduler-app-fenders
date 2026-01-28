package com.shiftscheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.ShiftDetailDTO;
import com.shiftscheduler.service.ShiftService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShiftController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("ShiftController Tests")
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShiftService shiftService;

    private ShiftDTO testShiftDTO;
    private ShiftDetailDTO testShiftDetailDTO;

    @BeforeEach
    void setUp() {
        testShiftDTO = new ShiftDTO();
        testShiftDTO.setId(1L);
        testShiftDTO.setName("Morning Shift");
        testShiftDTO.setDescription("Morning shift at brewery");
        testShiftDTO.setStartTime(LocalDateTime.of(2026, 2, 15, 8, 0));
        testShiftDTO.setEndTime(LocalDateTime.of(2026, 2, 15, 16, 0));
        testShiftDTO.setAvailableSlots(5);
        testShiftDTO.setFilledSlots(2);
        testShiftDTO.setActive(true);
        testShiftDTO.setCreatedById(1L);
        testShiftDTO.setCreatedByName("John Doe");

        testShiftDetailDTO = new ShiftDetailDTO();
        testShiftDetailDTO.setId(1L);
        testShiftDetailDTO.setName("Morning Shift");
        testShiftDetailDTO.setDescription("Morning shift at brewery");
        testShiftDetailDTO.setStartTime(LocalDateTime.of(2026, 2, 15, 8, 0));
        testShiftDetailDTO.setEndTime(LocalDateTime.of(2026, 2, 15, 16, 0));
        testShiftDetailDTO.setAvailableSlots(5);
        testShiftDetailDTO.setFilledSlots(2);
        testShiftDetailDTO.setActive(true);
        testShiftDetailDTO.setCreatedById(1L);
        testShiftDetailDTO.setCreatedByName("John Doe");
        testShiftDetailDTO.setSignups(Collections.emptyList());
    }

    @Nested
    @DisplayName("Create Shift Tests")
    class CreateShiftTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should create shift when authenticated as admin")
        void shouldCreateShiftWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            when(shiftService.createShift(any(ShiftDTO.class), eq(1L))).thenReturn(testShiftDTO);

            // When & Then
            mockMvc.perform(post("/api/shifts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testShiftDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Morning Shift"))
                    .andExpect(jsonPath("$.availableSlots").value(5));

            verify(shiftService).createShift(any(ShiftDTO.class), eq(1L));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to create shift")
        void shouldReturnForbiddenWhenNonAdminTriesToCreateShift() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/shifts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testShiftDTO)))
                    .andExpect(status().isForbidden());

            verify(shiftService, never()).createShift(any(), any());
        }
    }

    @Nested
    @DisplayName("Update Shift Tests")
    class UpdateShiftTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should update shift when authenticated as admin")
        void shouldUpdateShiftWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long shiftId = 1L;
            when(shiftService.updateShift(eq(shiftId), any(ShiftDTO.class), eq(1L))).thenReturn(testShiftDTO);

            // When & Then
            mockMvc.perform(put("/api/shifts/{shiftId}", shiftId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testShiftDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.id").value(1L));

            verify(shiftService).updateShift(eq(shiftId), any(ShiftDTO.class), eq(1L));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to update shift")
        void shouldReturnForbiddenWhenNonAdminTriesToUpdateShift() throws Exception {
            // Given
            Long shiftId = 1L;

            // When & Then
            mockMvc.perform(put("/api/shifts/{shiftId}", shiftId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testShiftDTO)))
                    .andExpect(status().isForbidden());

            verify(shiftService, never()).updateShift(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Delete Shift Tests")
    class DeleteShiftTests {

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"ADMIN"})
        @DisplayName("Should delete shift when authenticated as admin")
        void shouldDeleteShiftWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long shiftId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/shifts/{shiftId}", shiftId)
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(shiftService).deleteShift(eq(shiftId), eq(1L));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to delete shift")
        void shouldReturnForbiddenWhenNonAdminTriesToDeleteShift() throws Exception {
            // Given
            Long shiftId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/shifts/{shiftId}", shiftId)
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(shiftService, never()).deleteShift(any(), any());
        }
    }

    @Nested
    @DisplayName("Get Shift Tests")
    class GetShiftTests {

        @Test
        @DisplayName("Should get shift by ID")
        void shouldGetShiftById() throws Exception {
            // Given
            Long shiftId = 1L;
            when(shiftService.getShiftById(shiftId)).thenReturn(testShiftDTO);

            // When & Then
            mockMvc.perform(get("/api/shifts/{shiftId}", shiftId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Morning Shift"));

            verify(shiftService).getShiftById(shiftId);
        }

        @Test
        @DisplayName("Should get shift details by ID")
        void shouldGetShiftDetailsById() throws Exception {
            // Given
            Long shiftId = 1L;
            when(shiftService.getShiftDetails(shiftId)).thenReturn(testShiftDetailDTO);

            // When & Then
            mockMvc.perform(get("/api/shifts/{shiftId}/details", shiftId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.signups").isArray());

            verify(shiftService).getShiftDetails(shiftId);
        }
    }

    @Nested
    @DisplayName("Get Available Shifts Tests")
    class GetAvailableShiftsTests {

        @Test
        @DisplayName("Should get available shifts with default pagination")
        void shouldGetAvailableShiftsWithDefaultPagination() throws Exception {
            // Given
            List<ShiftDTO> shifts = Arrays.asList(testShiftDTO);
            Page<ShiftDTO> shiftPage = new PageImpl<>(shifts, PageRequest.of(0, 10), 1);
            when(shiftService.getAvailableShifts(any(Pageable.class))).thenReturn(shiftPage);

            // When & Then
            mockMvc.perform(get("/api/shifts/available"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(shiftService).getAvailableShifts(eq(PageRequest.of(0, 10)));
        }

        @Test
        @DisplayName("Should get available shifts with custom pagination")
        void shouldGetAvailableShiftsWithCustomPagination() throws Exception {
            // Given
            List<ShiftDTO> shifts = Arrays.asList(testShiftDTO);
            Page<ShiftDTO> shiftPage = new PageImpl<>(shifts, PageRequest.of(1, 5), 1);
            when(shiftService.getAvailableShifts(any(Pageable.class))).thenReturn(shiftPage);

            // When & Then
            mockMvc.perform(get("/api/shifts/available")
                    .param("page", "1")
                    .param("size", "5"))
                    .andExpect(status().isOk());

            verify(shiftService).getAvailableShifts(eq(PageRequest.of(1, 5)));
        }
    }

    @Nested
    @DisplayName("Get Upcoming Shifts Tests")
    class GetUpcomingShiftsTests {

        @Test
        @DisplayName("Should get upcoming shifts")
        void shouldGetUpcomingShifts() throws Exception {
            // Given
            List<ShiftDTO> shifts = Arrays.asList(testShiftDTO);
            Page<ShiftDTO> shiftPage = new PageImpl<>(shifts, PageRequest.of(0, 10), 1);
            when(shiftService.getUpcomingShifts(any(Pageable.class))).thenReturn(shiftPage);

            // When & Then
            mockMvc.perform(get("/api/shifts/upcoming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(shiftService).getUpcomingShifts(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Get Shifts By User Tests")
    class GetShiftsByUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get shifts by user when authenticated as admin")
        void shouldGetShiftsByUserWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            List<ShiftDTO> shifts = Arrays.asList(testShiftDTO);
            Page<ShiftDTO> shiftPage = new PageImpl<>(shifts, PageRequest.of(0, 10), 1);
            when(shiftService.getShiftsByUser(eq(userId), any(Pageable.class))).thenReturn(shiftPage);

            // When & Then
            mockMvc.perform(get("/api/shifts/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(shiftService).getShiftsByUser(eq(userId), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to get shifts by user")
        void shouldReturnForbiddenWhenNonAdminTriesToGetShiftsByUser() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(get("/api/shifts/user/{userId}", userId))
                    .andExpect(status().isForbidden());

            verify(shiftService, never()).getShiftsByUser(any(), any());
        }
    }
}
