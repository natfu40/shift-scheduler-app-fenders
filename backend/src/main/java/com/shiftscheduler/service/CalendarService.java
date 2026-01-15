package com.shiftscheduler.service;

import com.shiftscheduler.model.ShiftAssignment;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String generateUserShiftsICS(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        List<ShiftAssignment> approvedAssignments = shiftAssignmentRepository.findByUserId(userId)
                .stream()
                .filter(ShiftAssignment::isAccepted)
                .toList();

        StringBuilder ics = new StringBuilder();

        // ICS Header
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Fenders Brewing//Shift Scheduler//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:Fenders Brewing - Work Shifts\r\n");
        ics.append("X-WR-CALDESC:Your approved work shifts at Fenders Brewing\r\n");
        ics.append("X-WR-TIMEZONE:UTC\r\n");

        // Add each shift as an event
        for (ShiftAssignment assignment : approvedAssignments) {
            ics.append(createEventFromAssignment(assignment));
        }

        // ICS Footer
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    private String createEventFromAssignment(ShiftAssignment assignment) {
        StringBuilder event = new StringBuilder();

        // Convert LocalDateTime to UTC ZonedDateTime for formatting
        // Assuming shift times are stored in system timezone and need to be converted to UTC
        ZonedDateTime startTime = assignment.getShift().getStartTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endTime = assignment.getShift().getEndTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // Generate unique ID for the event
        String eventId = "shift-" + assignment.getId() + "@fendersbrewing.com";

        event.append("BEGIN:VEVENT\r\n");
        event.append("UID:").append(eventId).append("\r\n");
        event.append("DTSTART:").append(startTime.format(ICS_DATE_FORMAT)).append("\r\n");
        event.append("DTEND:").append(endTime.format(ICS_DATE_FORMAT)).append("\r\n");
        event.append("DTSTAMP:").append(now.format(ICS_DATE_FORMAT)).append("\r\n");
        event.append("SUMMARY:").append(escapeICSText(assignment.getShift().getName())).append("\r\n");

        if (assignment.getShift().getDescription() != null && !assignment.getShift().getDescription().trim().isEmpty()) {
            event.append("DESCRIPTION:").append(escapeICSText(assignment.getShift().getDescription())).append("\r\n");
        }

        event.append("LOCATION:Fenders Brewing\r\n");
        event.append("STATUS:CONFIRMED\r\n");
        event.append("TRANSP:OPAQUE\r\n");
        event.append("CATEGORIES:Work,Shift\r\n");

        // Add alarm 30 minutes before shift
        event.append("BEGIN:VALARM\r\n");
        event.append("TRIGGER:-PT30M\r\n");
        event.append("ACTION:DISPLAY\r\n");
        event.append("DESCRIPTION:Shift starts in 30 minutes: ").append(escapeICSText(assignment.getShift().getName())).append("\r\n");
        event.append("END:VALARM\r\n");

        event.append("END:VEVENT\r\n");

        return event.toString();
    }

    private String escapeICSText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(",", "\\,")
                   .replace(";", "\\;")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
}
