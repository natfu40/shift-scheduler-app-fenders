package com.shiftscheduler.security;

import com.shiftscheduler.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKeyForTesting123456789012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L); // 1 day

        testUser = createTestUser();
        UserPrincipal userPrincipal = UserPrincipal.create(testUser, Collections.emptyList());
        authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidJwtToken() {
        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject null token")
    void shouldRejectNullToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty token")
    void shouldRejectEmptyToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for same user at different times")
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // Given
        String token1 = jwtTokenProvider.generateToken(authentication);
        Thread.sleep(1000); // Wait 1 second

        // When
        String token2 = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUserIdFromToken(token1))
                .isEqualTo(jwtTokenProvider.getUserIdFromToken(token2));
    }

    @Test
    @DisplayName("Should handle expired token gracefully")
    void shouldHandleExpiredTokenGracefully() {
        // Given - Create provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", "testSecretKeyForTesting123456789012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpirationMs", 1L); // 1ms expiration

        String token = shortExpirationProvider.generateToken(authentication);

        // When - Wait for token to expire and validate
        try {
            Thread.sleep(10); // Wait for expiration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = shortExpirationProvider.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }
}
