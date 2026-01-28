package com.shiftscheduler.service;

import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.ShiftDetailDTO;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.mapper.DTOMapper;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.ShiftRepository;
import com.shiftscheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftService Unit Tests")
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private ShiftService shiftService;

    private User testUser;
    private Shift testShift;
    private ShiftDTO testShiftDTO;
    private ShiftDetailDTO testShiftDetailDTO;
    private ShiftAssignment testAssignment;
    private ShiftAssignmentDTO testAssignmentDTO;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testShift = new Shift();
        testShift.setId(1L);
        testShift.setName("Morning Shift");
        testShift.setDescription("Morning shift at brewery");
        testShift.setStartTime(LocalDateTime.of(2026, 2, 15, 8, 0));
        testShift.setEndTime(LocalDateTime.of(2026, 2, 15, 16, 0));
        testShift.setAvailableSlots(5);
        testShift.setFilledSlots(2);
        testShift.setActive(true);
        testShift.setCreatedBy(testUser);

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

        testAssignment = new ShiftAssignment();
        testAssignment.setId(1L);
        testAssignment.setShift(testShift);
        testAssignment.setUser(testUser);
        testAssignment.setAccepted(true);

        testAssignmentDTO = new ShiftAssignmentDTO();
        testAssignmentDTO.setId(1L);
        testAssignmentDTO.setShiftId(1L);
        testAssignmentDTO.setUserId(1L);
        testAssignmentDTO.setAccepted(true);

        testPageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Create Shift Tests")
    class CreateShiftTests {

        @Test
        @DisplayName("Should successfully create shift")
        void shouldSuccessfullyCreateShift() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When
            ShiftDTO result = shiftService.createShift(testShiftDTO, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testShiftDTO);

            ArgumentCaptor<Shift> shiftCaptor = ArgumentCaptor.forClass(Shift.class);
            verify(shiftRepository).save(shiftCaptor.capture());

            Shift savedShift = shiftCaptor.getValue();
            assertThat(savedShift.getName()).isEqualTo("Morning Shift");
            assertThat(savedShift.getDescription()).isEqualTo("Morning shift at brewery");
            assertThat(savedShift.getCreatedBy()).isEqualTo(testUser);
            assertThat(savedShift.isActive()).isTrue();

            verify(auditLogService).logAction(testUser, "CREATE_SHIFT", "Shift", testShift.getId(),
                    "Created shift: Morning Shift");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.createShift(testShiftDTO, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(shiftRepository, never()).save(any(Shift.class));
            verify(auditLogService, never()).logAction(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should create shift with all properties from DTO")
        void shouldCreateShiftWithAllPropertiesFromDTO() {
            // Given
            Long userId = 1L;
            ShiftDTO customShiftDTO = new ShiftDTO();
            customShiftDTO.setName("Evening Shift");
            customShiftDTO.setDescription("Special evening shift");
            customShiftDTO.setStartTime(LocalDateTime.of(2026, 3, 1, 18, 0));
            customShiftDTO.setEndTime(LocalDateTime.of(2026, 3, 2, 2, 0));
            customShiftDTO.setAvailableSlots(3);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);
            when(dtoMapper.toShiftDTO(any(Shift.class))).thenReturn(customShiftDTO);

            // When
            shiftService.createShift(customShiftDTO, userId);

            // Then
            ArgumentCaptor<Shift> shiftCaptor = ArgumentCaptor.forClass(Shift.class);
            verify(shiftRepository).save(shiftCaptor.capture());

            Shift savedShift = shiftCaptor.getValue();
            assertThat(savedShift.getName()).isEqualTo("Evening Shift");
            assertThat(savedShift.getDescription()).isEqualTo("Special evening shift");
            assertThat(savedShift.getStartTime()).isEqualTo(LocalDateTime.of(2026, 3, 1, 18, 0));
            assertThat(savedShift.getEndTime()).isEqualTo(LocalDateTime.of(2026, 3, 2, 2, 0));
            assertThat(savedShift.getAvailableSlots()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Update Shift Tests")
    class UpdateShiftTests {

        @Test
        @DisplayName("Should successfully update shift")
        void shouldSuccessfullyUpdateShift() {
            // Given
            Long shiftId = 1L;
            Long userId = 1L;

            ShiftDTO updateDTO = new ShiftDTO();
            updateDTO.setName("Updated Morning Shift");
            updateDTO.setDescription("Updated description");
            updateDTO.setStartTime(LocalDateTime.of(2026, 2, 16, 9, 0));
            updateDTO.setEndTime(LocalDateTime.of(2026, 2, 16, 17, 0));
            updateDTO.setAvailableSlots(8);
            updateDTO.setActive(false);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftRepository.save(testShift)).thenReturn(testShift);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(updateDTO);

            // When
            ShiftDTO result = shiftService.updateShift(shiftId, updateDTO, userId);

            // Then
            assertThat(result).isEqualTo(updateDTO);

            // Verify shift was updated
            assertThat(testShift.getName()).isEqualTo("Updated Morning Shift");
            assertThat(testShift.getDescription()).isEqualTo("Updated description");
            assertThat(testShift.getStartTime()).isEqualTo(LocalDateTime.of(2026, 2, 16, 9, 0));
            assertThat(testShift.getEndTime()).isEqualTo(LocalDateTime.of(2026, 2, 16, 17, 0));
            assertThat(testShift.getAvailableSlots()).isEqualTo(8);
            assertThat(testShift.isActive()).isFalse();

            verify(shiftRepository).save(testShift);
            verify(auditLogService).logAction(testUser, "UPDATE_SHIFT", "Shift", shiftId,
                    "Updated shift: Updated Morning Shift");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when shift not found")
        void shouldThrowResourceNotFoundExceptionWhenShiftNotFound() {
            // Given
            Long shiftId = 999L;
            Long userId = 1L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.updateShift(shiftId, testShiftDTO, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shift not found");

            verify(userRepository, never()).findById(any());
            verify(shiftRepository, never()).save(any());
            verify(auditLogService, never()).logAction(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFoundForUpdate() {
            // Given
            Long shiftId = 1L;
            Long userId = 999L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.updateShift(shiftId, testShiftDTO, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(shiftRepository, never()).save(any());
            verify(auditLogService, never()).logAction(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should not update filledSlots during update")
        void shouldNotUpdateFilledSlotsDuringUpdate() {
            // Given
            Long shiftId = 1L;
            Long userId = 1L;
            int originalFilledSlots = testShift.getFilledSlots();

            ShiftDTO updateDTO = new ShiftDTO();
            updateDTO.setName("Updated Shift");
            updateDTO.setFilledSlots(100); // This should be ignored
            updateDTO.setAvailableSlots(10);
            updateDTO.setActive(true);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftRepository.save(testShift)).thenReturn(testShift);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When
            shiftService.updateShift(shiftId, updateDTO, userId);

            // Then
            // FilledSlots should not have changed
            assertThat(testShift.getFilledSlots()).isEqualTo(originalFilledSlots);
        }
    }

    @Nested
    @DisplayName("Delete Shift Tests")
    class DeleteShiftTests {

        @Test
        @DisplayName("Should successfully delete shift with no assignments")
        void shouldSuccessfullyDeleteShiftWithNoAssignments() {
            // Given
            Long shiftId = 1L;
            Long userId = 1L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByShiftId(shiftId)).thenReturn(Collections.emptyList());

            // When
            shiftService.deleteShift(shiftId, userId);

            // Then
            verify(shiftAssignmentRepository).findByShiftId(shiftId);
            verify(shiftAssignmentRepository, never()).deleteByShiftId(shiftId);
            verify(shiftRepository).delete(testShift);
            verify(auditLogService).logAction(testUser, "DELETE_SHIFT", "Shift", shiftId,
                    "Deleted shift: Morning Shift");
        }

        @Test
        @DisplayName("Should delete shift assignments before deleting shift")
        void shouldDeleteShiftAssignmentsBeforeDeletingShift() {
            // Given
            Long shiftId = 1L;
            Long userId = 1L;
            List<ShiftAssignment> assignments = Arrays.asList(testAssignment);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByShiftId(shiftId)).thenReturn(assignments);

            // When
            shiftService.deleteShift(shiftId, userId);

            // Then
            verify(shiftAssignmentRepository).findByShiftId(shiftId);
            verify(shiftAssignmentRepository).deleteByShiftId(shiftId);
            verify(shiftRepository).delete(testShift);

            verify(auditLogService).logAction(testUser, "DELETE_SHIFT_ASSIGNMENTS", "ShiftAssignment", shiftId,
                    "Deleted 1 assignment(s) for shift: Morning Shift");
            verify(auditLogService).logAction(testUser, "DELETE_SHIFT", "Shift", shiftId,
                    "Deleted shift: Morning Shift");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when shift not found")
        void shouldThrowResourceNotFoundExceptionWhenShiftNotFoundForDelete() {
            // Given
            Long shiftId = 999L;
            Long userId = 1L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.deleteShift(shiftId, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shift not found");

            verify(userRepository, never()).findById(any());
            verify(shiftAssignmentRepository, never()).findByShiftId(any());
            verify(shiftRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFoundForDelete() {
            // Given
            Long shiftId = 1L;
            Long userId = 999L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.deleteShift(shiftId, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(shiftAssignmentRepository, never()).findByShiftId(any());
            verify(shiftRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should handle multiple assignments deletion")
        void shouldHandleMultipleAssignmentsDeletion() {
            // Given
            Long shiftId = 1L;
            Long userId = 1L;

            ShiftAssignment assignment2 = new ShiftAssignment();
            assignment2.setId(2L);
            List<ShiftAssignment> assignments = Arrays.asList(testAssignment, assignment2);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByShiftId(shiftId)).thenReturn(assignments);

            // When
            shiftService.deleteShift(shiftId, userId);

            // Then
            verify(auditLogService).logAction(testUser, "DELETE_SHIFT_ASSIGNMENTS", "ShiftAssignment", shiftId,
                    "Deleted 2 assignment(s) for shift: Morning Shift");
        }
    }

    @Nested
    @DisplayName("Get Shift By ID Tests")
    class GetShiftByIdTests {

        @Test
        @DisplayName("Should successfully get shift by ID")
        void shouldSuccessfullyGetShiftById() {
            // Given
            Long shiftId = 1L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When
            ShiftDTO result = shiftService.getShiftById(shiftId);

            // Then
            assertThat(result).isEqualTo(testShiftDTO);
            verify(shiftRepository).findById(shiftId);
            verify(dtoMapper).toShiftDTO(testShift);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when shift not found")
        void shouldThrowResourceNotFoundExceptionWhenShiftNotFoundById() {
            // Given
            Long shiftId = 999L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.getShiftById(shiftId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shift not found");

            verify(dtoMapper, never()).toShiftDTO(any());
        }
    }

    @Nested
    @DisplayName("Get Available Shifts Tests")
    class GetAvailableShiftsTests {

        @Test
        @DisplayName("Should return page of available shifts")
        void shouldReturnPageOfAvailableShifts() {
            // Given
            List<Shift> shifts = Arrays.asList(testShift);
            Page<Shift> shiftPage = new PageImpl<>(shifts, testPageable, 1);
            Page<ShiftDTO> expectedPage = shiftPage.map(dtoMapper::toShiftDTO);

            when(shiftRepository.findByActiveTrue(testPageable)).thenReturn(shiftPage);

            // When
            Page<ShiftDTO> result = shiftService.getAvailableShifts(testPageable);

            // Then
            assertThat(result).isNotNull();
            verify(shiftRepository).findByActiveTrue(testPageable);
        }

        @Test
        @DisplayName("Should return empty page when no available shifts")
        void shouldReturnEmptyPageWhenNoAvailableShifts() {
            // Given
            Page<Shift> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
            when(shiftRepository.findByActiveTrue(testPageable)).thenReturn(emptyPage);

            // When
            Page<ShiftDTO> result = shiftService.getAvailableShifts(testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(shiftRepository).findByActiveTrue(testPageable);
        }
    }

    @Nested
    @DisplayName("Get Upcoming Shifts Tests")
    class GetUpcomingShiftsTests {

        @Test
        @DisplayName("Should return page of upcoming shifts")
        void shouldReturnPageOfUpcomingShifts() {
            // Given
            List<Shift> shifts = Arrays.asList(testShift);
            Page<Shift> shiftPage = new PageImpl<>(shifts, testPageable, 1);

            try (MockedStatic<LocalDateTime> mockedDateTime = mockStatic(LocalDateTime.class)) {
                LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 28, 10, 0);
                mockedDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

                when(shiftRepository.findByActiveTrueAndStartTimeAfter(fixedNow, testPageable))
                        .thenReturn(shiftPage);

                // When
                Page<ShiftDTO> result = shiftService.getUpcomingShifts(testPageable);

                // Then
                assertThat(result).isNotNull();
                verify(shiftRepository).findByActiveTrueAndStartTimeAfter(fixedNow, testPageable);
            }
        }

        @Test
        @DisplayName("Should return empty page when no upcoming shifts")
        void shouldReturnEmptyPageWhenNoUpcomingShifts() {
            // Given
            Page<Shift> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

            try (MockedStatic<LocalDateTime> mockedDateTime = mockStatic(LocalDateTime.class)) {
                LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 28, 10, 0);
                mockedDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

                when(shiftRepository.findByActiveTrueAndStartTimeAfter(fixedNow, testPageable))
                        .thenReturn(emptyPage);

                // When
                Page<ShiftDTO> result = shiftService.getUpcomingShifts(testPageable);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Get Shifts By User Tests")
    class GetShiftsByUserTests {

        @Test
        @DisplayName("Should return paginated shifts created by user")
        void shouldReturnPaginatedShiftsCreatedByUser() {
            // Given
            Long userId = 1L;
            List<Shift> shifts = Arrays.asList(testShift);
            when(shiftRepository.findByCreatedById(userId)).thenReturn(shifts);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When
            Page<ShiftDTO> result = shiftService.getShiftsByUser(userId, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testShiftDTO);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(shiftRepository).findByCreatedById(userId);
            verify(dtoMapper).toShiftDTO(testShift);
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            Long userId = 1L;
            Pageable smallPage = PageRequest.of(0, 1);

            // Create 3 shifts
            Shift shift2 = new Shift();
            shift2.setId(2L);
            shift2.setName("Shift 2");

            Shift shift3 = new Shift();
            shift3.setId(3L);
            shift3.setName("Shift 3");

            List<Shift> allShifts = Arrays.asList(testShift, shift2, shift3);

            ShiftDTO shiftDTO2 = new ShiftDTO();
            shiftDTO2.setId(2L);
            shiftDTO2.setName("Shift 2");

            when(shiftRepository.findByCreatedById(userId)).thenReturn(allShifts);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);
            when(dtoMapper.toShiftDTO(shift2)).thenReturn(shiftDTO2);

            // When
            Page<ShiftDTO> result = shiftService.getShiftsByUser(userId, smallPage);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return empty page when user has no shifts")
        void shouldReturnEmptyPageWhenUserHasNoShifts() {
            // Given
            Long userId = 1L;
            when(shiftRepository.findByCreatedById(userId)).thenReturn(Collections.emptyList());

            // When
            Page<ShiftDTO> result = shiftService.getShiftsByUser(userId, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Get Shift Details Tests")
    class GetShiftDetailsTests {

        @Test
        @DisplayName("Should return shift details with signups")
        void shouldReturnShiftDetailsWithSignups() {
            // Given
            Long shiftId = 1L;
            List<ShiftAssignment> assignments = Arrays.asList(testAssignment);
            List<ShiftAssignmentDTO> assignmentDTOs = Arrays.asList(testAssignmentDTO);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(shiftAssignmentRepository.findByShiftId(shiftId)).thenReturn(assignments);
            when(dtoMapper.toAssignmentDTO(testAssignment)).thenReturn(testAssignmentDTO);

            // When
            ShiftDetailDTO result = shiftService.getShiftDetails(shiftId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Morning Shift");
            assertThat(result.getDescription()).isEqualTo("Morning shift at brewery");
            assertThat(result.getAvailableSlots()).isEqualTo(5);
            assertThat(result.getFilledSlots()).isEqualTo(2);
            assertThat(result.getCreatedById()).isEqualTo(1L);
            assertThat(result.getCreatedByName()).isEqualTo("John Doe");
            assertThat(result.getSignups()).hasSize(1);
            assertThat(result.getSignups().get(0)).isEqualTo(testAssignmentDTO);

            verify(shiftRepository).findById(shiftId);
            verify(shiftAssignmentRepository).findByShiftId(shiftId);
            verify(dtoMapper).toAssignmentDTO(testAssignment);
        }

        @Test
        @DisplayName("Should return shift details with empty signups list")
        void shouldReturnShiftDetailsWithEmptySignupsList() {
            // Given
            Long shiftId = 1L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(shiftAssignmentRepository.findByShiftId(shiftId)).thenReturn(Collections.emptyList());

            // When
            ShiftDetailDTO result = shiftService.getShiftDetails(shiftId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSignups()).isEmpty();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when shift not found")
        void shouldThrowResourceNotFoundExceptionWhenShiftNotFoundForDetails() {
            // Given
            Long shiftId = 999L;
            when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftService.getShiftDetails(shiftId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shift not found");

            verify(shiftAssignmentRepository, never()).findByShiftId(any());
        }

        @Test
        @DisplayName("Should handle multiple signups correctly")
        void shouldHandleMultipleSignupsCorrectly() {
            // Given
            Long shiftId = 1L;

            ShiftAssignment assignment2 = new ShiftAssignment();
            assignment2.setId(2L);

            ShiftAssignmentDTO assignmentDTO2 = new ShiftAssignmentDTO();
            assignmentDTO2.setId(2L);

            List<ShiftAssignment> assignments = Arrays.asList(testAssignment, assignment2);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(shiftAssignmentRepository.findByShiftId(shiftId)).thenReturn(assignments);
            when(dtoMapper.toAssignmentDTO(testAssignment)).thenReturn(testAssignmentDTO);
            when(dtoMapper.toAssignmentDTO(assignment2)).thenReturn(assignmentDTO2);

            // When
            ShiftDetailDTO result = shiftService.getShiftDetails(shiftId);

            // Then
            assertThat(result.getSignups()).hasSize(2);
            assertThat(result.getSignups()).containsExactly(testAssignmentDTO, assignmentDTO2);
        }
    }

    @Nested
    @DisplayName("Pagination Helper Tests")
    class PaginationHelperTests {

        @Test
        @DisplayName("Should create page from list correctly")
        void shouldCreatePageFromListCorrectly() {
            // Given
            Long userId = 1L;
            ShiftDTO shift2 = new ShiftDTO();
            shift2.setId(2L);
            shift2.setName("Shift 2");

            ShiftDTO shift3 = new ShiftDTO();
            shift3.setId(3L);
            shift3.setName("Shift 3");

            List<Shift> allShifts = Arrays.asList(testShift, new Shift(), new Shift());
            Pageable page1 = PageRequest.of(1, 1); // Second page, size 1

            when(shiftRepository.findByCreatedById(userId)).thenReturn(allShifts);
            when(dtoMapper.toShiftDTO(any(Shift.class))).thenReturn(testShiftDTO, shift2, shift3);

            // When
            Page<ShiftDTO> result = shiftService.getShiftsByUser(userId, page1);

            // Then
            assertThat(result.getNumber()).isEqualTo(1); // Page number
            assertThat(result.getSize()).isEqualTo(1);   // Page size
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle edge case when requesting page beyond available data")
        void shouldHandleEdgeCaseWhenRequestingPageBeyondAvailableData() {
            // Given
            Long userId = 1L;
            List<Shift> shifts = Arrays.asList(testShift); // Only 1 shift available
            Pageable beyondPage = PageRequest.of(2, 1); // Request page 2 with size 1, but only 1 item total

            when(shiftRepository.findByCreatedById(userId)).thenReturn(shifts);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When
            Page<ShiftDTO> result = shiftService.getShiftsByUser(userId, beyondPage);

            // Then - Should return empty content since we're requesting beyond available data
            assertThat(result.getContent()).isEmpty(); // Page 2 is beyond available data (only page 0 has data)
            assertThat(result.getTotalElements()).isEqualTo(1); // Total elements is still 1
            assertThat(result.getNumber()).isEqualTo(2); // We're on page 2
            assertThat(result.getSize()).isEqualTo(1); // Page size is 1
            assertThat(result.getTotalPages()).isEqualTo(1); // Only 1 page total
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should verify exact repository interactions for all methods")
        void shouldVerifyExactRepositoryInteractions() {
            // Given
            Long shiftId = 1L;

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When
            shiftService.getShiftById(shiftId);

            // Then
            verify(shiftRepository, times(1)).findById(shiftId);
            verify(dtoMapper, times(1)).toShiftDTO(testShift);
            verifyNoMoreInteractions(shiftRepository, userRepository, shiftAssignmentRepository,
                    auditLogService, dtoMapper);
        }

        @Test
        @DisplayName("Should handle null values in shift creation gracefully")
        void shouldHandleNullValuesInShiftCreationGracefully() {
            // Given
            Long userId = 1L;
            ShiftDTO nullDescriptionDTO = new ShiftDTO();
            nullDescriptionDTO.setName("Test Shift");
            nullDescriptionDTO.setDescription(null); // null description
            nullDescriptionDTO.setStartTime(LocalDateTime.of(2026, 3, 1, 10, 0));
            nullDescriptionDTO.setEndTime(LocalDateTime.of(2026, 3, 1, 18, 0));
            nullDescriptionDTO.setAvailableSlots(5);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);
            when(dtoMapper.toShiftDTO(any(Shift.class))).thenReturn(nullDescriptionDTO);

            // When
            ShiftDTO result = shiftService.createShift(nullDescriptionDTO, userId);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<Shift> shiftCaptor = ArgumentCaptor.forClass(Shift.class);
            verify(shiftRepository).save(shiftCaptor.capture());

            Shift savedShift = shiftCaptor.getValue();
            assertThat(savedShift.getDescription()).isNull();
            assertThat(savedShift.getName()).isEqualTo("Test Shift");
        }

        @Test
        @DisplayName("Should handle concurrent modifications gracefully")
        void shouldHandleConcurrentModificationsGracefully() {
            // Given
            Long shiftId = 1L;
            Long userId = 1L;

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(testShift));
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftRepository.save(testShift)).thenReturn(testShift);
            when(dtoMapper.toShiftDTO(testShift)).thenReturn(testShiftDTO);

            // When - simulate multiple updates
            shiftService.updateShift(shiftId, testShiftDTO, userId);
            shiftService.updateShift(shiftId, testShiftDTO, userId);

            // Then
            verify(shiftRepository, times(2)).save(testShift);
            verify(auditLogService, times(2)).logAction(eq(testUser), eq("UPDATE_SHIFT"),
                    eq("Shift"), eq(shiftId), anyString());
        }
    }
}
