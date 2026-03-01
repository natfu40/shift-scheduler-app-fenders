package com.shiftscheduler.service;

import com.shiftscheduler.dto.AdminResponse;
import com.shiftscheduler.model.Role;
import com.shiftscheduler.model.User;
import com.shiftscheduler.model.UserRole;
import com.shiftscheduler.repository.RoleRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Unit Tests")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private AdminService adminService;

    private User testUser;
    private Role adminRole;
    private Role userRole;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role with full access");

        userRole = new Role();
        userRole.setId(2L);
        userRole.setName("USER");
        userRole.setDescription("Standard user role");

        testUserRole = new UserRole();
        testUserRole.setId(1L);
        testUserRole.setUser(testUser);
        testUserRole.setRole(adminRole);
    }

    @Nested
    @DisplayName("Assign Admin Role Tests")
    class AssignAdminRoleTests {

        @Test
        @DisplayName("Should successfully assign admin role to user without admin role")
        void shouldSuccessfullyAssignAdminRoleToUser() {
            // Given
            Long userId = 1L;
            UserRole nonAdminUserRole = new UserRole();
            nonAdminUserRole.setUser(testUser);
            nonAdminUserRole.setRole(userRole);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(nonAdminUserRole));
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

            // When
            AdminResponse response = adminService.assignAdminRole(userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getMessage()).isEqualTo("User promoted to ADMIN");
            assertThat(response.isSuccess()).isTrue();

            verify(userRepository).findById(userId);
            verify(userRoleRepository).findByUserId(userId);
            verify(roleRepository).findByName("ADMIN");
            verify(userRoleRepository).save(any(UserRole.class));
        }

        @Test
        @DisplayName("Should return message when user already has admin role")
        void shouldReturnMessageWhenUserAlreadyHasAdminRole() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(testUserRole));

            // When
            AdminResponse response = adminService.assignAdminRole(userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getMessage()).isEqualTo("User already has ADMIN role");
            assertThat(response.isSuccess()).isTrue();

            verify(userRepository).findById(userId);
            verify(userRoleRepository).findByUserId(userId);
            verify(roleRepository, never()).findByName(anyString());
            verify(userRoleRepository, never()).save(any(UserRole.class));
        }

        @Test
        @DisplayName("Should create admin role if it doesn't exist")
        void shouldCreateAdminRoleIfNotExists() {
            // Given
            Long userId = 1L;
            UserRole nonAdminUserRole = new UserRole();
            nonAdminUserRole.setUser(testUser);
            nonAdminUserRole.setRole(userRole);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(nonAdminUserRole));
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenReturn(adminRole);
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

            // When
            AdminResponse response = adminService.assignAdminRole(userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getMessage()).isEqualTo("User promoted to ADMIN");
            assertThat(response.isSuccess()).isTrue();

            verify(roleRepository).findByName("ADMIN");
            verify(roleRepository).save(argThat(role ->
                "ADMIN".equals(role.getName()) &&
                "Administrator role with full access".equals(role.getDescription())
            ));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminService.assignAdminRole(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(userRepository).findById(userId);
            verify(userRoleRepository, never()).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Should handle user with multiple roles including admin")
        void shouldHandleUserWithMultipleRolesIncludingAdmin() {
            // Given
            Long userId = 1L;
            UserRole regularRole = new UserRole();
            regularRole.setUser(testUser);
            regularRole.setRole(userRole);

            List<UserRole> multipleRoles = Arrays.asList(regularRole, testUserRole);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(multipleRoles);

            // When
            AdminResponse response = adminService.assignAdminRole(userId);

            // Then
            assertThat(response.getMessage()).isEqualTo("User already has ADMIN role");
            assertThat(response.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Remove Admin Role Tests")
    class RemoveAdminRoleTests {

        @Test
        @DisplayName("Should successfully remove admin role from user")
        void shouldSuccessfullyRemoveAdminRoleFromUser() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(testUserRole));

            // When
            AdminResponse response = adminService.removeAdminRole(userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getMessage()).isEqualTo("ADMIN role removed from user");
            assertThat(response.isSuccess()).isTrue();

            verify(userRepository).findById(userId);
            verify(userRoleRepository).findByUserId(userId);
            verify(userRoleRepository).delete(testUserRole);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFoundForRemoval() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminService.removeAdminRole(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(userRepository).findById(userId);
            verify(userRoleRepository, never()).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when user does not have admin role")
        void shouldThrowExceptionWhenUserDoesNotHaveAdminRole() {
            // Given
            Long userId = 1L;
            UserRole nonAdminRole = new UserRole();
            nonAdminRole.setUser(testUser);
            nonAdminRole.setRole(userRole);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(nonAdminRole));

            // When & Then
            assertThatThrownBy(() -> adminService.removeAdminRole(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User does not have ADMIN role");

            verify(userRepository).findById(userId);
            verify(userRoleRepository).findByUserId(userId);
            verify(userRoleRepository, never()).delete(any(UserRole.class));
        }

        @Test
        @DisplayName("Should remove admin role when user has multiple roles")
        void shouldRemoveAdminRoleWhenUserHasMultipleRoles() {
            // Given
            Long userId = 1L;
            UserRole regularRole = new UserRole();
            regularRole.setUser(testUser);
            regularRole.setRole(userRole);

            List<UserRole> multipleRoles = Arrays.asList(regularRole, testUserRole);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(multipleRoles);

            // When
            AdminResponse response = adminService.removeAdminRole(userId);

            // Then
            assertThat(response.getMessage()).isEqualTo("ADMIN role removed from user");
            verify(userRoleRepository).delete(testUserRole);
            verify(userRoleRepository, never()).delete(regularRole);
        }
    }

    @Nested
    @DisplayName("Is User Admin Tests")
    class IsUserAdminTests {

        @Test
        @DisplayName("Should return true when user has admin role")
        void shouldReturnTrueWhenUserHasAdminRole() {
            // Given
            Long userId = 1L;
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(testUserRole));

            // When
            boolean isAdmin = adminService.isUserAdmin(userId);

            // Then
            assertThat(isAdmin).isTrue();
            verify(userRoleRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return false when user does not have admin role")
        void shouldReturnFalseWhenUserDoesNotHaveAdminRole() {
            // Given
            Long userId = 1L;
            UserRole nonAdminRole = new UserRole();
            nonAdminRole.setUser(testUser);
            nonAdminRole.setRole(userRole);

            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(nonAdminRole));

            // When
            boolean isAdmin = adminService.isUserAdmin(userId);

            // Then
            assertThat(isAdmin).isFalse();
            verify(userRoleRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return false when user has no roles")
        void shouldReturnFalseWhenUserHasNoRoles() {
            // Given
            Long userId = 1L;
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

            // When
            boolean isAdmin = adminService.isUserAdmin(userId);

            // Then
            assertThat(isAdmin).isFalse();
            verify(userRoleRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return true when user has admin role among multiple roles")
        void shouldReturnTrueWhenUserHasAdminRoleAmongMultiple() {
            // Given
            Long userId = 1L;
            UserRole regularRole = new UserRole();
            regularRole.setUser(testUser);
            regularRole.setRole(userRole);

            Role moderatorRole = new Role();
            moderatorRole.setName("MODERATOR");
            UserRole modRole = new UserRole();
            modRole.setUser(testUser);
            modRole.setRole(moderatorRole);

            List<UserRole> multipleRoles = Arrays.asList(regularRole, modRole, testUserRole);

            when(userRoleRepository.findByUserId(userId)).thenReturn(multipleRoles);

            // When
            boolean isAdmin = adminService.isUserAdmin(userId);

            // Then
            assertThat(isAdmin).isTrue();
            verify(userRoleRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should handle case-sensitive role matching")
        void shouldHandleCaseSensitiveRoleMatching() {
            // Given
            Long userId = 1L;
            Role lowercaseAdminRole = new Role();
            lowercaseAdminRole.setName("admin"); // lowercase
            UserRole lowercaseRole = new UserRole();
            lowercaseRole.setUser(testUser);
            lowercaseRole.setRole(lowercaseAdminRole);

            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(lowercaseRole));

            // When
            boolean isAdmin = adminService.isUserAdmin(userId);

            // Then
            assertThat(isAdmin).isFalse(); // Should be false since it's case-sensitive
            verify(userRoleRepository).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void shouldHandleNullUserIdGracefully() {
            // When & Then
            assertThatThrownBy(() -> adminService.assignAdminRole(null))
                    .isInstanceOf(Exception.class);

            assertThatThrownBy(() -> adminService.removeAdminRole(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should verify repository interactions for assign admin role")
        void shouldVerifyRepositoryInteractionsForAssignAdminRole() {
            // Given
            Long userId = 1L;
            UserRole nonAdminUserRole = new UserRole();
            nonAdminUserRole.setUser(testUser);
            nonAdminUserRole.setRole(userRole);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(nonAdminUserRole));
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

            // When
            adminService.assignAdminRole(userId);

            // Then - Verify exact interactions
            verify(userRepository, times(1)).findById(userId);
            verify(userRoleRepository, times(1)).findByUserId(userId);
            verify(roleRepository, times(1)).findByName("ADMIN");
            verify(userRoleRepository, times(1)).save(any(UserRole.class));
            verifyNoMoreInteractions(userRepository, roleRepository, userRoleRepository);
        }

        @Test
        @DisplayName("Should verify repository interactions for remove admin role")
        void shouldVerifyRepositoryInteractionsForRemoveAdminRole() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.singletonList(testUserRole));

            // When
            adminService.removeAdminRole(userId);

            // Then - Verify exact interactions
            verify(userRepository, times(1)).findById(userId);
            verify(userRoleRepository, times(1)).findByUserId(userId);
            verify(userRoleRepository, times(1)).delete(testUserRole);
            verifyNoMoreInteractions(userRepository, roleRepository, userRoleRepository);
        }
    }
}
