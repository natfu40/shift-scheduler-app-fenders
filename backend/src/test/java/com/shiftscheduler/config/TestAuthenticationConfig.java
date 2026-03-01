package com.shiftscheduler.config;

import com.shiftscheduler.security.UserPrincipal;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * Configuration for creating proper test authentication contexts
 */
@TestConfiguration
public class TestAuthenticationConfig {

    /**
     * Custom annotation for mocking users with proper authentication principal
     */
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = TestAuthenticationConfig.WithMockAppUserSecurityContextFactory.class)
    public @interface WithMockAppUser {
        long id() default 1L;

        String username() default "test@example.com";

        String[] roles() default {"USER"};
    }

    /**
     * Factory for creating security contexts with proper authentication objects
     */
    public static class WithMockAppUserSecurityContextFactory implements WithSecurityContextFactory<WithMockAppUser> {

        @Override
        public SecurityContext createSecurityContext(WithMockAppUser annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            // Create actual UserPrincipal that the controllers expect
            UserPrincipal principal = new UserPrincipal(
                    annotation.id(),
                    annotation.username(),
                    "test-password",
                    Arrays.stream(annotation.roles())
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList()
            );

            PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            auth.setAuthenticated(true);
            context.setAuthentication(auth);
            return context;
        }
    }
}
