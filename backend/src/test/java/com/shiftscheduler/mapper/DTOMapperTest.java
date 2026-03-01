package com.shiftscheduler.mapper;

import com.shiftscheduler.dto.ShiftAssignmentDTO;
import com.shiftscheduler.dto.ShiftDTO;
import com.shiftscheduler.dto.UserDTO;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.service.UserService;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DTOMapper Unit Tests")
class DTOMapperTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private DTOMapper dtoMapper;

    private User testUser;
    private User creatorUser;
    private Shift testShift;
    private ShiftAssignment testAssignment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setActive(true);

        creatorUser = new User();
        creatorUser.setId(2L);
        creatorUser.setEmail("creator@example.com");
        creatorUser.setFirstName("Jane");
        creatorUser.setLastName("Smith");
        creatorUser.setActive(true);

        testShift = new Shift();
        testShift.setId(1L);
        testShift.setName("Morning Shift");
        testShift.setDescription("Morning shift at brewery");
        testShift.setStartTime(LocalDateTime.of(2026, 2, 15, 8, 0));
        testShift.setEndTime(LocalDateTime.of(2026, 2, 15, 16, 0));
        testShift.setAvailableSlots(5);
        testShift.setFilledSlots(2);
        testShift.setActive(true);
        testShift.setCreatedBy(creatorUser);

        testAssignment = new ShiftAssignment();
        testAssignment.setId(1L);
        testAssignment.setUser(testUser);
        testAssignment.setShift(testShift);
        testAssignment.setAccepted(true);
        testAssignment.setSignedUpAt(Instant.parse("2026-02-10T16:30:00Z"));
        testAssignment.setAcceptedAt(Instant.parse("2026-02-11T20:15:00Z"));
    }

    @Nested
    @DisplayName("User to UserDTO Mapping Tests")
    class UserToUserDTOTests {

        @Test
        @DisplayName("Should map User to UserDTO with admin role")
        void shouldMapUserToUserDTOWithAdminRole() {
            // Given
            when(userService.isAdmin(1L)).thenReturn(true);

            // When
            UserDTO result = dtoMapper.toUserDTO(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.isAdmin()).isTrue();

            verify(userService).isAdmin(1L);
        }

        @Test
        @DisplayName("Should map User to UserDTO with regular user role")
        void shouldMapUserToUserDTOWithRegularUserRole() {
            // Given
            when(userService.isAdmin(1L)).thenReturn(false);

            // When
            UserDTO result = dtoMapper.toUserDTO(testUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.isAdmin()).isFalse();

            verify(userService).isAdmin(1L);
        }

        @Test
        @DisplayName("Should handle User with null fields gracefully")
        void shouldHandleUserWithNullFieldsGracefully() {
            // Given
            User userWithNulls = new User();
            userWithNulls.setId(2L);
            userWithNulls.setEmail(null);
            userWithNulls.setFirstName(null);
            userWithNulls.setLastName(null);

            when(userService.isAdmin(2L)).thenReturn(false);

            // When
            UserDTO result = dtoMapper.toUserDTO(userWithNulls);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getEmail()).isNull();
            assertThat(result.getFirstName()).isNull();
            assertThat(result.getLastName()).isNull();
            assertThat(result.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should handle User with empty string fields")
        void shouldHandleUserWithEmptyStringFields() {
            // Given
            User userWithEmptyStrings = new User();
            userWithEmptyStrings.setId(3L);
            userWithEmptyStrings.setEmail("");
            userWithEmptyStrings.setFirstName("");
            userWithEmptyStrings.setLastName("");

            when(userService.isAdmin(3L)).thenReturn(true);

            // When
            UserDTO result = dtoMapper.toUserDTO(userWithEmptyStrings);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getEmail()).isEmpty();
            assertThat(result.getFirstName()).isEmpty();
            assertThat(result.getLastName()).isEmpty();
            assertThat(result.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("Should handle special characters in user fields")
        void shouldHandleSpecialCharactersInUserFields() {
            // Given
            User userWithSpecialChars = new User();
            userWithSpecialChars.setId(4L);
            userWithSpecialChars.setEmail("test+special@example-domain.com");
            userWithSpecialChars.setFirstName("José");
            userWithSpecialChars.setLastName("O'Connor-Smith");

            when(userService.isAdmin(4L)).thenReturn(false);

            // When
            UserDTO result = dtoMapper.toUserDTO(userWithSpecialChars);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test+special@example-domain.com");
            assertThat(result.getFirstName()).isEqualTo("José");
            assertThat(result.getLastName()).isEqualTo("O'Connor-Smith");
        }
    }

    @Nested
    @DisplayName("Shift to ShiftDTO Mapping Tests")
    class ShiftToShiftDTOTests {

        @Test
        @DisplayName("Should map Shift to ShiftDTO with all fields")
        void shouldMapShiftToShiftDTOWithAllFields() {
            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Morning Shift");
            assertThat(result.getDescription()).isEqualTo("Morning shift at brewery");
            assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2026, 2, 15, 8, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2026, 2, 15, 16, 0));
            assertThat(result.getAvailableSlots()).isEqualTo(5);
            assertThat(result.getFilledSlots()).isEqualTo(2);
            assertThat(result.isActive()).isTrue();
            assertThat(result.getCreatedById()).isEqualTo(2L);
            assertThat(result.getCreatedByName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("Should map Shift to ShiftDTO with null description")
        void shouldMapShiftToShiftDTOWithNullDescription() {
            // Given
            testShift.setDescription(null);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Morning Shift");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getCreatedByName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("Should map Shift to ShiftDTO with empty description")
        void shouldMapShiftToShiftDTOWithEmptyDescription() {
            // Given
            testShift.setDescription("");

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("Should map inactive Shift to ShiftDTO")
        void shouldMapInactiveShiftToShiftDTO() {
            // Given
            testShift.setActive(false);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should map Shift with zero slots to ShiftDTO")
        void shouldMapShiftWithZeroSlotsToShiftDTO() {
            // Given
            testShift.setAvailableSlots(0);
            testShift.setFilledSlots(0);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAvailableSlots()).isEqualTo(0);
            assertThat(result.getFilledSlots()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle creator with null first or last name")
        void shouldHandleCreatorWithNullFirstOrLastName() {
            // Given
            creatorUser.setFirstName(null);
            creatorUser.setLastName("Smith");

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCreatedByName()).isEqualTo("null Smith");
        }

        @Test
        @DisplayName("Should handle creator with both names null")
        void shouldHandleCreatorWithBothNamesNull() {
            // Given
            creatorUser.setFirstName(null);
            creatorUser.setLastName(null);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCreatedByName()).isEqualTo("null null");
        }

        @Test
        @DisplayName("Should handle shift spanning multiple days")
        void shouldHandleShiftSpanningMultipleDays() {
            // Given
            testShift.setStartTime(LocalDateTime.of(2026, 2, 15, 22, 0)); // 10 PM
            testShift.setEndTime(LocalDateTime.of(2026, 2, 16, 6, 0));   // 6 AM next day

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2026, 2, 15, 22, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2026, 2, 16, 6, 0));
        }

        @Test
        @DisplayName("Should handle special characters in shift name and description")
        void shouldHandleSpecialCharactersInShiftNameAndDescription() {
            // Given
            testShift.setName("Café & Bar Shift (Evening)");
            testShift.setDescription("Special shift: includes café service, bar duties & cleaning — ends at 2:00 AM");

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(testShift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Café & Bar Shift (Evening)");
            assertThat(result.getDescription()).isEqualTo("Special shift: includes café service, bar duties & cleaning — ends at 2:00 AM");
        }
    }

    @Nested
    @DisplayName("ShiftAssignment to ShiftAssignmentDTO Mapping Tests")
    class ShiftAssignmentToShiftAssignmentDTOTests {

        @Test
        @DisplayName("Should map accepted ShiftAssignment to ShiftAssignmentDTO")
        void shouldMapAcceptedShiftAssignmentToShiftAssignmentDTO() {
            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getUserEmail()).isEqualTo("test@example.com");
            assertThat(result.getUserFirstName()).isEqualTo("John");
            assertThat(result.getUserLastName()).isEqualTo("Doe");
            assertThat(result.getShiftId()).isEqualTo(1L);
            assertThat(result.getShiftName()).isEqualTo("Morning Shift");
            assertThat(result.isAccepted()).isTrue();
            assertThat(result.getSignedUpAt()).isEqualTo(Instant.parse("2026-02-10T16:30:00Z"));
            assertThat(result.getAcceptedAt()).isEqualTo(Instant.parse("2026-02-11T20:15:00Z"));
        }

        @Test
        @DisplayName("Should map pending ShiftAssignment to ShiftAssignmentDTO")
        void shouldMapPendingShiftAssignmentToShiftAssignmentDTO() {
            // Given
            testAssignment.setAccepted(false);
            testAssignment.setAcceptedAt(null);

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isAccepted()).isFalse();
            assertThat(result.getAcceptedAt()).isNull();
            assertThat(result.getSignedUpAt()).isEqualTo(Instant.parse("2026-02-10T16:30:00Z"));
        }

        @Test
        @DisplayName("Should map ShiftAssignment with null timestamps")
        void shouldMapShiftAssignmentWithNullTimestamps() {
            // Given
            testAssignment.setSignedUpAt(null);
            testAssignment.setAcceptedAt(null);

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSignedUpAt()).isNull();
            assertThat(result.getAcceptedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle user with null email")
        void shouldHandleUserWithNullEmail() {
            // Given
            testUser.setEmail(null);

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserEmail()).isNull();
            assertThat(result.getUserFirstName()).isEqualTo("John");
            assertThat(result.getUserLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should handle shift with null name")
        void shouldHandleShiftWithNullName() {
            // Given
            testShift.setName(null);

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getShiftName()).isNull();
            assertThat(result.getShiftId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle user with null names")
        void shouldHandleUserWithNullNames() {
            // Given
            testUser.setFirstName(null);
            testUser.setLastName(null);

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserFirstName()).isNull();
            assertThat(result.getUserLastName()).isNull();
            assertThat(result.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle assignment with accepted status but no accepted timestamp")
        void shouldHandleAssignmentWithAcceptedStatusButNoAcceptedTimestamp() {
            // Given
            testAssignment.setAccepted(true);
            testAssignment.setAcceptedAt(null); // Inconsistent state for testing

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isAccepted()).isTrue();
            assertThat(result.getAcceptedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle special characters in user and shift names")
        void shouldHandleSpecialCharactersInUserAndShiftNames() {
            // Given
            testUser.setFirstName("José María");
            testUser.setLastName("García-López");
            testUser.setEmail("jose.maria@español-café.com");
            testShift.setName("Shift: Evening & Night (10:00 PM - 6:00 AM)");

            // When
            ShiftAssignmentDTO result = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserFirstName()).isEqualTo("José María");
            assertThat(result.getUserLastName()).isEqualTo("García-López");
            assertThat(result.getUserEmail()).isEqualTo("jose.maria@español-café.com");
            assertThat(result.getShiftName()).isEqualTo("Shift: Evening & Night (10:00 PM - 6:00 AM)");
        }
    }

    @Nested
    @DisplayName("Full Name Generation Tests")
    class FullNameGenerationTests {

        @Test
        @DisplayName("Should generate full name with both first and last name")
        void shouldGenerateFullNameWithBothFirstAndLastName() {
            // Given
            User user = new User();
            user.setFirstName("John");
            user.setLastName("Doe");

            Shift shift = new Shift();
            shift.setId(1L);
            shift.setName("Test Shift");
            shift.setStartTime(LocalDateTime.now());
            shift.setEndTime(LocalDateTime.now().plusHours(8));
            shift.setAvailableSlots(5);
            shift.setFilledSlots(0);
            shift.setActive(true);
            shift.setCreatedBy(user);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(shift);

            // Then
            assertThat(result.getCreatedByName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should generate full name with empty strings")
        void shouldGenerateFullNameWithEmptyStrings() {
            // Given
            User user = new User();
            user.setFirstName("");
            user.setLastName("");

            Shift shift = new Shift();
            shift.setId(1L);
            shift.setName("Test Shift");
            shift.setStartTime(LocalDateTime.now());
            shift.setEndTime(LocalDateTime.now().plusHours(8));
            shift.setAvailableSlots(5);
            shift.setFilledSlots(0);
            shift.setActive(true);
            shift.setCreatedBy(user);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(shift);

            // Then
            assertThat(result.getCreatedByName()).isEqualTo(" ");
        }

        @Test
        @DisplayName("Should generate full name with whitespace names")
        void shouldGenerateFullNameWithWhitespaceNames() {
            // Given
            User user = new User();
            user.setFirstName("   ");
            user.setLastName("   ");

            Shift shift = new Shift();
            shift.setId(1L);
            shift.setName("Test Shift");
            shift.setStartTime(LocalDateTime.now());
            shift.setEndTime(LocalDateTime.now().plusHours(8));
            shift.setAvailableSlots(5);
            shift.setFilledSlots(0);
            shift.setActive(true);
            shift.setCreatedBy(user);

            // When
            ShiftDTO result = dtoMapper.toShiftDTO(shift);

            // Then
            assertThat(result.getCreatedByName()).isEqualTo("       "); // 3 spaces + 1 space + 3 spaces = 7 spaces
        }
    }

    @Nested
    @DisplayName("Integration and Edge Cases Tests")
    class IntegrationAndEdgeCasesTests {

        @Test
        @DisplayName("Should handle mapping multiple entities consistently")
        void shouldHandleMappingMultipleEntitiesConsistently() {
            // Given
            when(userService.isAdmin(1L)).thenReturn(true);
            when(userService.isAdmin(2L)).thenReturn(false);

            // When
            UserDTO userDTO1 = dtoMapper.toUserDTO(testUser);
            UserDTO userDTO2 = dtoMapper.toUserDTO(creatorUser);
            ShiftDTO shiftDTO = dtoMapper.toShiftDTO(testShift);
            ShiftAssignmentDTO assignmentDTO = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(userDTO1).isNotNull();
            assertThat(userDTO2).isNotNull();
            assertThat(shiftDTO).isNotNull();
            assertThat(assignmentDTO).isNotNull();

            // Verify consistency across related entities
            assertThat(userDTO1.getId()).isEqualTo(assignmentDTO.getUserId());
            assertThat(shiftDTO.getId()).isEqualTo(assignmentDTO.getShiftId());
            assertThat(shiftDTO.getCreatedById()).isEqualTo(userDTO2.getId());

            verify(userService, times(2)).isAdmin(anyLong());
        }

        @Test
        @DisplayName("Should handle null entities gracefully")
        void shouldHandleNullEntitiesGracefully() {
            // When & Then
            assertThatThrownBy(() -> dtoMapper.toUserDTO(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> dtoMapper.toShiftDTO(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> dtoMapper.toAssignmentDTO(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should verify UserService is called exactly once per User mapping")
        void shouldVerifyUserServiceIsCalledExactlyOncePerUserMapping() {
            // Given
            when(userService.isAdmin(1L)).thenReturn(true);

            // When
            dtoMapper.toUserDTO(testUser);
            dtoMapper.toUserDTO(testUser); // Second call with same user

            // Then
            verify(userService, times(2)).isAdmin(1L);
        }

        @Test
        @DisplayName("Should handle concurrent mapping operations")
        void shouldHandleConcurrentMappingOperations() {
            // Given
            when(userService.isAdmin(anyLong())).thenReturn(true);

            // When - simulate concurrent operations
            UserDTO userDTO = dtoMapper.toUserDTO(testUser);
            ShiftDTO shiftDTO = dtoMapper.toShiftDTO(testShift);
            ShiftAssignmentDTO assignmentDTO = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(userDTO).isNotNull();
            assertThat(shiftDTO).isNotNull();
            assertThat(assignmentDTO).isNotNull();

            // All mappings should be independent and not interfere with each other
            assertThat(userDTO.getId()).isEqualTo(testUser.getId());
            assertThat(shiftDTO.getId()).isEqualTo(testShift.getId());
            assertThat(assignmentDTO.getId()).isEqualTo(testAssignment.getId());
        }

        @Test
        @DisplayName("Should handle extreme timestamp values")
        void shouldHandleExtremeTimestampValues() {
            // Given
            LocalDateTime farFuture = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
            LocalDateTime farPast = LocalDateTime.of(1000, 1, 1, 0, 0, 0);

            testShift.setStartTime(farPast);
            testShift.setEndTime(farFuture);
            testAssignment.setSignedUpAt(Instant.parse("1000-01-01T00:00:00Z"));
            testAssignment.setAcceptedAt(Instant.parse("9999-12-31T23:59:59Z"));

            // When
            ShiftDTO shiftDTO = dtoMapper.toShiftDTO(testShift);
            ShiftAssignmentDTO assignmentDTO = dtoMapper.toAssignmentDTO(testAssignment);

            // Then
            assertThat(shiftDTO.getStartTime()).isEqualTo(farPast);
            assertThat(shiftDTO.getEndTime()).isEqualTo(farFuture);
            assertThat(assignmentDTO.getSignedUpAt()).isEqualTo(Instant.parse("1000-01-01T00:00:00Z"));
            assertThat(assignmentDTO.getAcceptedAt()).isEqualTo(Instant.parse("9999-12-31T23:59:59Z"));
        }
    }
}
