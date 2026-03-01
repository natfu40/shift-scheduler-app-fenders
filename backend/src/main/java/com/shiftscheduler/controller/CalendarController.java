package com.shiftscheduler.controller;

import com.shiftscheduler.service.CalendarService;
import com.shiftscheduler.security.UserPrincipal;
import com.shiftscheduler.security.JwtTokenProvider;
import com.shiftscheduler.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/user/{userId}/shifts.ics")
    public ResponseEntity<String> getUserShiftsICS(@PathVariable Long userId, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Users can only access their own calendar or admins can access any
        if (!userPrincipal.getId().equals(userId) && !userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String icsContent = calendarService.generateUserShiftsICS(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.set("Content-Disposition", "attachment; filename=my-shifts.ics");
        headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.set("Pragma", "no-cache");
        headers.set("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent);
    }

    @GetMapping("/user/my-shifts.ics")
    public ResponseEntity<String> getMyShiftsICS(
            Authentication authentication,
            @RequestParam(required = false) String token) {

        UserPrincipal userPrincipal;

        // If token is provided in URL parameter (for calendar subscription), validate it
        if (token != null && authentication == null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);
                    userPrincipal = (UserPrincipal) userDetails;
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else if (authentication != null) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String icsContent = calendarService.generateUserShiftsICS(userPrincipal.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.set("Content-Disposition", "inline; filename=my-shifts.ics");
        headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.set("Pragma", "no-cache");
        headers.set("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent);
    }
}
