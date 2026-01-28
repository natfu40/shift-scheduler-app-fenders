package com.shiftscheduler.service;

import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.model.Shift;
import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService Unit Tests")
class CalendarServiceTest {

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CalendarService calendarService;

    private User testUser;
    private Shift testShift;
    private ShiftAssignment acceptedAssignment;
    private ShiftAssignment pendingAssignment;
    private ShiftAssignment rejectedAssignment;

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
        testShift.setStartTime(LocalDateTime.of(2026, 2, 15, 8, 0)); // 8:00 AM
        testShift.setEndTime(LocalDateTime.of(2026, 2, 15, 16, 0)); // 4:00 PM

        acceptedAssignment = new ShiftAssignment();
        acceptedAssignment.setId(1L);
        acceptedAssignment.setShift(testShift);
        acceptedAssignment.setUser(testUser);
        acceptedAssignment.setAccepted(true);

        pendingAssignment = new ShiftAssignment();
        pendingAssignment.setId(2L);
        pendingAssignment.setShift(testShift);
        pendingAssignment.setUser(testUser);
        pendingAssignment.setAccepted(false);

        rejectedAssignment = new ShiftAssignment();
        rejectedAssignment.setId(3L);
        rejectedAssignment.setShift(testShift);
        rejectedAssignment.setUser(testUser);
        rejectedAssignment.setAccepted(false);
    }

    @Nested
    @DisplayName("Generate User Shifts ICS Tests")
    class GenerateUserShiftsICSTests {

        @Test
        @DisplayName("Should generate valid ICS calendar for user with accepted shifts")
        void shouldGenerateValidICSCalendarForUserWithAcceptedShifts() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Arrays.asList(acceptedAssignment, pendingAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).isNotNull();

            // Verify ICS structure
            assertThat(icsContent).startsWith("BEGIN:VCALENDAR\r\n");
            assertThat(icsContent).endsWith("END:VCALENDAR\r\n");

            // Verify calendar properties
            assertThat(icsContent).contains("VERSION:2.0\r\n");
            assertThat(icsContent).contains("PRODID:-//Fenders Brewing//Shift Scheduler//EN\r\n");
            assertThat(icsContent).contains("CALSCALE:GREGORIAN\r\n");
            assertThat(icsContent).contains("METHOD:PUBLISH\r\n");
            assertThat(icsContent).contains("X-WR-CALNAME:Fenders Brewing - Work Shifts\r\n");
            assertThat(icsContent).contains("X-WR-CALDESC:Your approved work shifts at Fenders Brewing\r\n");
            assertThat(icsContent).contains("X-WR-TIMEZONE:UTC\r\n");

            // Verify event is included (only accepted assignment should be included)
            assertThat(icsContent).contains("BEGIN:VEVENT\r\n");
            assertThat(icsContent).contains("END:VEVENT\r\n");
            assertThat(icsContent).contains("SUMMARY:Morning Shift\r\n");
            assertThat(icsContent).contains("UID:shift-1@fendersbrewing.com\r\n");

            // Count events - should only have 1 accepted assignment
            long eventCount = icsContent.lines().filter(line -> line.equals("BEGIN:VEVENT")).count();
            assertThat(eventCount).isEqualTo(1);

            verify(userRepository).findById(userId);
            verify(shiftAssignmentRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should generate ICS with proper date format")
        void shouldGenerateICSWithProperDateFormat() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            // Date should be in format yyyyMMddTHHmmssZ (UTC)
            assertThat(icsContent).containsPattern("DTSTART:\\d{8}T\\d{6}Z\r\n");
            assertThat(icsContent).containsPattern("DTEND:\\d{8}T\\d{6}Z\r\n");
            assertThat(icsContent).containsPattern("DTSTAMP:\\d{8}T\\d{6}Z\r\n");
        }

        @Test
        @DisplayName("Should include shift description when present")
        void shouldIncludeShiftDescriptionWhenPresent() {
            // Given
            Long userId = 1L;
            testShift.setDescription("Important morning shift with special duties");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).contains("DESCRIPTION:Important morning shift with special duties\r\n");
        }

        @Test
        @DisplayName("Should not include description when null")
        void shouldNotIncludeDescriptionWhenNull() {
            // Given
            Long userId = 1L;
            testShift.setDescription(null);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            // Should not contain shift description as a standalone DESCRIPTION line in VEVENT
            // (VALARM will still have its own DESCRIPTION line which is expected)
            String[] lines = icsContent.split("\r\n");
            boolean foundEventDescriptionLine = false;
            boolean inVEvent = false;
            boolean inVAlarm = false;

            for (String line : lines) {
                if (line.equals("BEGIN:VEVENT")) {
                    inVEvent = true;
                } else if (line.equals("END:VEVENT")) {
                    inVEvent = false;
                } else if (line.equals("BEGIN:VALARM")) {
                    inVAlarm = true;
                } else if (line.equals("END:VALARM")) {
                    inVAlarm = false;
                } else if (line.startsWith("DESCRIPTION:") && inVEvent && !inVAlarm) {
                    // This is a DESCRIPTION line in VEVENT but not in VALARM
                    foundEventDescriptionLine = true;
                }
            }

            assertThat(foundEventDescriptionLine).isFalse();
        }

        @Test
        @DisplayName("Should not include description when empty")
        void shouldNotIncludeDescriptionWhenEmpty() {
            // Given
            Long userId = 1L;
            testShift.setDescription("   "); // Empty/whitespace
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            // Should not contain shift description as a standalone DESCRIPTION line in VEVENT
            // (VALARM will still have its own DESCRIPTION line which is expected)
            String[] lines = icsContent.split("\r\n");
            boolean foundEventDescriptionLine = false;
            boolean inVEvent = false;
            boolean inVAlarm = false;

            for (String line : lines) {
                if (line.equals("BEGIN:VEVENT")) {
                    inVEvent = true;
                } else if (line.equals("END:VEVENT")) {
                    inVEvent = false;
                } else if (line.equals("BEGIN:VALARM")) {
                    inVAlarm = true;
                } else if (line.equals("END:VALARM")) {
                    inVAlarm = false;
                } else if (line.startsWith("DESCRIPTION:") && inVEvent && !inVAlarm) {
                    // This is a DESCRIPTION line in VEVENT but not in VALARM
                    foundEventDescriptionLine = true;
                }
            }

            assertThat(foundEventDescriptionLine).isFalse();
        }

        @Test
        @DisplayName("Should generate empty calendar when user has no accepted shifts")
        void shouldGenerateEmptyCalendarWhenUserHasNoAcceptedShifts() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Arrays.asList(pendingAssignment, rejectedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).isNotNull();
            assertThat(icsContent).startsWith("BEGIN:VCALENDAR\r\n");
            assertThat(icsContent).endsWith("END:VCALENDAR\r\n");

            // Should not contain any events
            assertThat(icsContent).doesNotContain("BEGIN:VEVENT");
            assertThat(icsContent).doesNotContain("END:VEVENT");
        }

        @Test
        @DisplayName("Should generate calendar with multiple accepted shifts")
        void shouldGenerateCalendarWithMultipleAcceptedShifts() {
            // Given
            Long userId = 1L;

            // Create second shift and assignment
            Shift eveningShift = new Shift();
            eveningShift.setId(2L);
            eveningShift.setName("Evening Shift");
            eveningShift.setDescription("Evening shift at brewery");
            eveningShift.setStartTime(LocalDateTime.of(2026, 2, 16, 17, 0));
            eveningShift.setEndTime(LocalDateTime.of(2026, 2, 17, 1, 0));

            ShiftAssignment secondAcceptedAssignment = new ShiftAssignment();
            secondAcceptedAssignment.setId(4L);
            secondAcceptedAssignment.setShift(eveningShift);
            secondAcceptedAssignment.setUser(testUser);
            secondAcceptedAssignment.setAccepted(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Arrays.asList(acceptedAssignment, secondAcceptedAssignment, pendingAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            // Should contain both accepted shifts
            long eventCount = icsContent.lines().filter(line -> line.equals("BEGIN:VEVENT")).count();
            assertThat(eventCount).isEqualTo(2);

            assertThat(icsContent).contains("SUMMARY:Morning Shift\r\n");
            assertThat(icsContent).contains("SUMMARY:Evening Shift\r\n");
            assertThat(icsContent).contains("UID:shift-1@fendersbrewing.com\r\n");
            assertThat(icsContent).contains("UID:shift-4@fendersbrewing.com\r\n");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarService.generateUserShiftsICS(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(userId);
            verify(shiftAssignmentRepository, never()).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Should handle user with no shift assignments")
        void shouldHandleUserWithNoShiftAssignments() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.emptyList());

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).isNotNull();
            assertThat(icsContent).contains("BEGIN:VCALENDAR\r\n");
            assertThat(icsContent).contains("END:VCALENDAR\r\n");
            assertThat(icsContent).doesNotContain("BEGIN:VEVENT");
        }
    }

    @Nested
    @DisplayName("ICS Event Generation Tests")
    class ICSEventGenerationTests {

        @Test
        @DisplayName("Should include all required event properties")
        void shouldIncludeAllRequiredEventProperties() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            // Verify all required VEVENT properties are present
            assertThat(icsContent).contains("UID:shift-1@fendersbrewing.com\r\n");
            assertThat(icsContent).contains("SUMMARY:Morning Shift\r\n");
            assertThat(icsContent).contains("LOCATION:Fenders Brewing\r\n");
            assertThat(icsContent).contains("STATUS:CONFIRMED\r\n");
            assertThat(icsContent).contains("TRANSP:OPAQUE\r\n");
            assertThat(icsContent).contains("CATEGORIES:Work,Shift\r\n");
        }

        @Test
        @DisplayName("Should include alarm 30 minutes before shift")
        void shouldIncludeAlarm30MinutesBeforeShift() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).contains("BEGIN:VALARM\r\n");
            assertThat(icsContent).contains("TRIGGER:-PT30M\r\n");
            assertThat(icsContent).contains("ACTION:DISPLAY\r\n");
            assertThat(icsContent).contains("DESCRIPTION:Shift starts in 30 minutes: Morning Shift\r\n");
            assertThat(icsContent).contains("END:VALARM\r\n");
        }

        @Test
        @DisplayName("Should properly escape special characters in text")
        void shouldProperlyEscapeSpecialCharactersInText() {
            // Given
            Long userId = 1L;
            testShift.setName("Test\\Shift;With,Special\nChars\rLine");
            testShift.setDescription("Description\\With;Special,Chars\nAnd\rReturns");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            // Verify escaping: \ -> \\, ; -> \;, , -> \,, \n -> \n, \r removed
            assertThat(icsContent).contains("SUMMARY:Test\\\\Shift\\;With\\,Special\\nCharsLine\r\n");
            assertThat(icsContent).contains("DESCRIPTION:Description\\\\With\\;Special\\,Chars\\nAndReturns\r\n");
            assertThat(icsContent).contains("DESCRIPTION:Shift starts in 30 minutes: Test\\\\Shift\\;With\\,Special\\nCharsLine\r\n");
        }

        @Test
        @DisplayName("Should handle null shift name")
        void shouldHandleNullShiftName() {
            // Given
            Long userId = 1L;
            testShift.setName(null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).contains("SUMMARY:\r\n");
            assertThat(icsContent).contains("DESCRIPTION:Shift starts in 30 minutes: \r\n");
        }

        @Test
        @DisplayName("Should generate unique UIDs for different assignments")
        void shouldGenerateUniqueUIDsForDifferentAssignments() {
            // Given
            Long userId = 1L;

            ShiftAssignment secondAssignment = new ShiftAssignment();
            secondAssignment.setId(5L);
            secondAssignment.setShift(testShift);
            secondAssignment.setUser(testUser);
            secondAssignment.setAccepted(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Arrays.asList(acceptedAssignment, secondAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).contains("UID:shift-1@fendersbrewing.com\r\n");
            assertThat(icsContent).contains("UID:shift-5@fendersbrewing.com\r\n");
        }
    }

    @Nested
    @DisplayName("Text Escaping Tests")
    class TextEscapingTests {

        @Test
        @DisplayName("Should handle empty and null strings in escaping")
        void shouldHandleEmptyAndNullStringsInEscaping() {
            // Given
            Long userId = 1L;
            testShift.setName("");
            testShift.setDescription("");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).contains("SUMMARY:\r\n");
            // Check that shift description is not included as a standalone DESCRIPTION line in VEVENT
            // (VALARM will still have its own DESCRIPTION line which is expected)
            String[] lines = icsContent.split("\r\n");
            boolean foundEventDescriptionLine = false;
            boolean inVEvent = false;
            boolean inVAlarm = false;

            for (String line : lines) {
                if (line.equals("BEGIN:VEVENT")) {
                    inVEvent = true;
                } else if (line.equals("END:VEVENT")) {
                    inVEvent = false;
                } else if (line.equals("BEGIN:VALARM")) {
                    inVAlarm = true;
                } else if (line.equals("END:VALARM")) {
                    inVAlarm = false;
                } else if (line.startsWith("DESCRIPTION:") && inVEvent && !inVAlarm) {
                    // This is a DESCRIPTION line in VEVENT but not in VALARM
                    foundEventDescriptionLine = true;
                }
            }

            assertThat(foundEventDescriptionLine).isFalse();
        }

        @Test
        @DisplayName("Should handle all special characters correctly")
        void shouldHandleAllSpecialCharactersCorrectly() {
            // Given
            Long userId = 1L;
            // Test string with all special characters that need escaping
            String testString = "Backslash\\Semicolon;Comma,NewLine\nCarriageReturn\rNormal";
            testShift.setName(testString);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            String expectedEscaped = "Backslash\\\\Semicolon\\;Comma\\,NewLine\\nCarriageReturnNormal";
            assertThat(icsContent).contains("SUMMARY:" + expectedEscaped + "\r\n");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle shifts spanning multiple days")
        void shouldHandleShiftsSpanningMultipleDays() {
            // Given
            Long userId = 1L;
            // Night shift that goes past midnight
            testShift.setStartTime(LocalDateTime.of(2026, 2, 15, 23, 0)); // 11:00 PM
            testShift.setEndTime(LocalDateTime.of(2026, 2, 16, 7, 0));   // 7:00 AM next day

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).isNotNull();
            assertThat(icsContent).contains("BEGIN:VEVENT\r\n");
            assertThat(icsContent).contains("END:VEVENT\r\n");
            // Should have valid date format even across day boundaries
            assertThat(icsContent).containsPattern("DTSTART:\\d{8}T\\d{6}Z\r\n");
            assertThat(icsContent).containsPattern("DTEND:\\d{8}T\\d{6}Z\r\n");
        }

        @Test
        @DisplayName("Should handle very large assignment lists efficiently")
        void shouldHandleVeryLargeAssignmentListsEfficiently() {
            // Given
            Long userId = 1L;
            List<ShiftAssignment> manyAssignments = Collections.nCopies(100, acceptedAssignment);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId)).thenReturn(manyAssignments);

            // When
            String icsContent = calendarService.generateUserShiftsICS(userId);

            // Then
            assertThat(icsContent).isNotNull();
            long eventCount = icsContent.lines().filter(line -> line.equals("BEGIN:VEVENT")).count();
            assertThat(eventCount).isEqualTo(100);
        }

        @Test
        @DisplayName("Should verify all repository interactions")
        void shouldVerifyAllRepositoryInteractions() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            calendarService.generateUserShiftsICS(userId);

            // Then
            verify(userRepository, times(1)).findById(userId);
            verify(shiftAssignmentRepository, times(1)).findByUserId(userId);
            verifyNoMoreInteractions(userRepository, shiftAssignmentRepository);
        }

        @Test
        @DisplayName("Should produce consistent output for same input")
        void shouldProduceConsistentOutputForSameInput() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(shiftAssignmentRepository.findByUserId(userId))
                    .thenReturn(Collections.singletonList(acceptedAssignment));

            // When
            String icsContent1 = calendarService.generateUserShiftsICS(userId);
            String icsContent2 = calendarService.generateUserShiftsICS(userId);

            // Then
            // Content should be identical except for DTSTAMP (which uses current time)
            String[] lines1 = icsContent1.split("\r\n");
            String[] lines2 = icsContent2.split("\r\n");

            assertThat(lines1).hasSameSizeAs(lines2);

            for (int i = 0; i < lines1.length; i++) {
                if (!lines1[i].startsWith("DTSTAMP:")) {
                    assertThat(lines1[i]).isEqualTo(lines2[i]);
                }
            }
        }
    }
}
