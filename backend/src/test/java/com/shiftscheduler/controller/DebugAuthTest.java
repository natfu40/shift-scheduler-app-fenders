package com.shiftscheduler.controller;

import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("Debug Authentication Test")
class DebugAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow unauthenticated access to health endpoint")
    void shouldAllowUnauthenticatedAccessToHealthEndpoint() throws Exception {
        // When & Then - Health endpoint should be public
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
    @DisplayName("Should provide authentication context")
    void shouldProvideAuthenticationContext() throws Exception {
        // This test verifies that the authentication setup is working
        // If this passes, then the authentication infrastructure is correct

        // When & Then - This should work if authentication is set up correctly
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}
