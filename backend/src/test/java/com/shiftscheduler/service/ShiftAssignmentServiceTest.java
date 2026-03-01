package com.shiftscheduler.service;

import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.ShiftRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftAssignmentService Tests")
class ShiftAssignmentServiceTest {

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ShiftAssignmentService shiftAssignmentService;

    private User testUser;
    private User adminUser;
    private Shift testShift;
    private ShiftAssignment testAssignment;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        adminUser = TestDataFactory.createAdminUser();
        testShift = createTestShift();
        testAssignment = createTestShiftAssignment();
    }

    private Shift createTestShift() {
        Shift shift = new Shift();
        shift.setId(1L);
        shift.setName("Morning Shift");
        shift.setDescription("Test morning shift");
        shift.setStartTime(LocalDateTime.now().plusDays(1));
        shift.setEndTime(LocalDateTime.now().plusDays(1).plusHours(8));
        shift.setActive(true);
        shift.setAvailableSlots(3);
        shift.setFilledSlots(1);
        shift.setCreatedBy(testUser);
        return shift;
    }

    private ShiftAssignment createTestShiftAssignment() {
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setId(1L);
        assignment.setShift(testShift);
        assignment.setUser(testUser);
        assignment.setAccepted(false);
        assignment.setSignedUpAt(Instant.now());
        return assignment;
    }

    @Nested
    @DisplayName("Sign Up for Shift Tests")
    class SignUpForShiftTests {

        @Test
        @DisplayName("Should successfully sign up user for available shift")
        void shouldSuccessfullySignUpUserForAvailableShift() {
            // Given
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(shiftRepository.findById(testShift.getId())).thenReturn(Optional.of(testShift));
            when(shiftAssignmentRepository.findByUserIdAndShiftId(testUser.getId(), testShift.getId()))
                    .thenReturn(Optional.empty());
            when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

            // When
            ShiftAssignmentDTO result = shiftAssignmentService.signupForShift(testShift.getId(), testUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUser.getId());
            assertThat(result.getShiftId()).isEqualTo(testShift.getId());
            verify(shiftAssignmentRepository).save(any(ShiftAssignment.class));
            verify(auditLogService).logAction(eq(testUser), eq("SIGNUP_SHIFT"), eq("ShiftAssignment"),
                    any(), contains("Signed up for shift"));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftAssignmentService.signupForShift(testShift.getId(), 999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");

            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when shift not found")
        void shouldThrowExceptionWhenShiftNotFound() {
            // Given
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftAssignmentService.signupForShift(999L, testUser.getId()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Shift not found");

            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user already signed up")
        void shouldThrowExceptionWhenUserAlreadySignedUp() {
            // Given
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(shiftRepository.findById(testShift.getId())).thenReturn(Optional.of(testShift));
            when(shiftAssignmentRepository.findByUserIdAndShiftId(testUser.getId(), testShift.getId()))
                    .thenReturn(Optional.of(testAssignment));

            // When & Then
            assertThatThrownBy(() -> shiftAssignmentService.signupForShift(testShift.getId(), testUser.getId()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already signed up");

            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when shift is full")
        void shouldThrowExceptionWhenShiftIsFull() {
            // Given
            testShift.setFilledSlots(testShift.getAvailableSlots()); // Make shift full
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(shiftRepository.findById(testShift.getId())).thenReturn(Optional.of(testShift));
            when(shiftAssignmentRepository.findByUserIdAndShiftId(testUser.getId(), testShift.getId()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftAssignmentService.signupForShift(testShift.getId(), testUser.getId()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("full");

            verify(shiftAssignmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Accept Signup Tests")
    class AcceptSignupTests {

        @Test
        @DisplayName("Should successfully accept signup")
        void shouldSuccessfullyAcceptSignup() {
            // Given
            when(shiftAssignmentRepository.findById(testAssignment.getId())).thenReturn(Optional.of(testAssignment));
            when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
            when(shiftRepository.save(testShift)).thenReturn(testShift);
            when(shiftAssignmentRepository.save(testAssignment)).thenReturn(testAssignment);

            // When
            ShiftAssignmentDTO result = shiftAssignmentService.acceptSignup(testAssignment.getId(), adminUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(testAssignment.isAccepted()).isTrue();
            assertThat(testAssignment.getAcceptedAt()).isNotNull();
            verify(shiftRepository).save(testShift);
            verify(shiftAssignmentRepository).save(testAssignment);
            verify(auditLogService).logAction(eq(adminUser), eq("ACCEPT_SIGNUP"), eq("ShiftAssignment"),
                    eq(testAssignment.getId()), contains("Accepted signup"));
        }

        @Test
        @DisplayName("Should throw exception when assignment not found")
        void shouldThrowExceptionWhenAssignmentNotFound() {
            // Given
            when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> shiftAssignmentService.acceptSignup(999L, adminUser.getId()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Assignment not found");
        }
    }

    @Nested
    @DisplayName("Reject Signup Tests")
    class RejectSignupTests {

        @Test
        @DisplayName("Should reject unaccepted signup without affecting filled slots")
        void shouldRejectUnacceptedSignupWithoutAffectingFilledSlots() {
            // Given
            testAssignment.setAccepted(false);
            when(shiftAssignmentRepository.findById(testAssignment.getId())).thenReturn(Optional.of(testAssignment));
            when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

            // When
            shiftAssignmentService.rejectSignup(testAssignment.getId(), adminUser.getId());

            // Then
            verify(shiftAssignmentRepository).delete(testAssignment);
            verify(shiftRepository, never()).save(any()); // Should not affect filled slots
            verify(auditLogService).logAction(eq(adminUser), eq("REJECT_SIGNUP"), eq("ShiftAssignment"),
                    eq(testAssignment.getId()), contains("Rejected signup"));
        }

        @Test
        @DisplayName("Should reject accepted signup and decrement filled slots")
        void shouldRejectAcceptedSignupAndDecrementFilledSlots() {
            // Given
            testAssignment.setAccepted(true);
            testShift.setFilledSlots(2);
            when(shiftAssignmentRepository.findById(testAssignment.getId())).thenReturn(Optional.of(testAssignment));
            when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

            // When
            shiftAssignmentService.rejectSignup(testAssignment.getId(), adminUser.getId());

            // Then
            verify(shiftRepository).save(testShift);
            assertThat(testShift.getFilledSlots()).isEqualTo(1); // Decremented
            verify(shiftAssignmentRepository).delete(testAssignment);
        }
    }

    @Nested
    @DisplayName("Get Methods Tests")
    class GetMethodsTests {

        @Test
        @DisplayName("Should get all signups for shift")
        void shouldGetAllSignupsForShift() {
            // Given
            User user2 = TestDataFactory.createTestUser(2L, "user2@example.com", "Jane", "Doe");
            ShiftAssignment assignment2 = createTestShiftAssignment();
            assignment2.setId(2L);
            assignment2.setUser(user2);

            List<ShiftAssignment> assignments = Arrays.asList(testAssignment, assignment2);
            when(shiftAssignmentRepository.findByShiftId(testShift.getId())).thenReturn(assignments);

            // When
            List<ShiftAssignmentDTO> result = shiftAssignmentService.getSignupsForShift(testShift.getId());

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo(testUser.getId());
            assertThat(result.get(1).getUserId()).isEqualTo(user2.getId());
        }

        @Test
        @DisplayName("Should get user's shift assignments")
        void shouldGetUsersShiftAssignments() {
            // Given
            List<ShiftAssignment> assignments = Arrays.asList(testAssignment);
            when(shiftAssignmentRepository.findByUserId(testUser.getId())).thenReturn(assignments);

            // When
            List<ShiftAssignmentDTO> result = shiftAssignmentService.getUserShiftAssignments(testUser.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(testUser.getId());
            assertThat(result.get(0).getShiftId()).isEqualTo(testShift.getId());
        }

        @Test
        @DisplayName("Should return empty list when no signups exist")
        void shouldReturnEmptyListWhenNoSignupsExist() {
            // Given
            when(shiftAssignmentRepository.findByShiftId(testShift.getId())).thenReturn(Arrays.asList());

            // When
            List<ShiftAssignmentDTO> result = shiftAssignmentService.getSignupsForShift(testShift.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("DTO Conversion Tests")
    class DTOConversionTests {

        @Test
        @DisplayName("Should convert ShiftAssignment to DTO correctly")
        void shouldConvertShiftAssignmentToDTOCorrectly() {
            // Given - Call a method that uses the private convertToDTO method
            when(shiftAssignmentRepository.findByShiftId(testShift.getId())).thenReturn(Arrays.asList(testAssignment));

            // When
            List<ShiftAssignmentDTO> result = shiftAssignmentService.getSignupsForShift(testShift.getId());

            // Then
            assertThat(result).hasSize(1);
            ShiftAssignmentDTO dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(testAssignment.getId());
            assertThat(dto.getUserId()).isEqualTo(testUser.getId());
            assertThat(dto.getUserEmail()).isEqualTo(testUser.getEmail());
            assertThat(dto.getUserFirstName()).isEqualTo(testUser.getFirstName());
            assertThat(dto.getUserLastName()).isEqualTo(testUser.getLastName());
            assertThat(dto.getShiftId()).isEqualTo(testShift.getId());
            assertThat(dto.getShiftName()).isEqualTo(testShift.getName());
            assertThat(dto.isAccepted()).isEqualTo(testAssignment.isAccepted());
            assertThat(dto.getSignedUpAt()).isEqualTo(testAssignment.getSignedUpAt());
        }
    }
}
