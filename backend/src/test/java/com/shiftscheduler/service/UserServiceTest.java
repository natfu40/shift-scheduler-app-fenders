package com.shiftscheduler.service;

import com.shiftscheduler.dto.CreateUserRequest;
import com.shiftscheduler.dto.UpdateUserRequest;
import com.shiftscheduler.dto.UserDTO;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.mapper.DTOMapper;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.AuditLogRepository;
import com.shiftscheduler.repository.ShiftAssignmentRepository;
import com.shiftscheduler.repository.UserRepository;
import com.shiftscheduler.repository.UserRoleRepository;
import com.shiftscheduler.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private DTOMapper dtoMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        testUserDTO = new UserDTO(1L, "test@example.com", "John", "Doe", false);
        createUserRequest = new CreateUserRequest("new@example.com", "sha256Hash", "Jane", "Smith");
        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("Updated");
        updateUserRequest.setLastName("Name");
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users as DTOs")
        void shouldReturnAllUsersAsDTOs() {
            // Given
            User user1 = TestDataFactory.createTestUser(1L, "user1@example.com", "User", "One");
            User user2 = TestDataFactory.createTestUser(2L, "user2@example.com", "User", "Two");
            List<User> users = Arrays.asList(user1, user2);

            UserDTO userDTO1 = new UserDTO(1L, "user1@example.com", "User", "One", false);
            UserDTO userDTO2 = new UserDTO(2L, "user2@example.com", "User", "Two", false);

            when(userRepository.findAll()).thenReturn(users);
            when(dtoMapper.toUserDTO(user1)).thenReturn(userDTO1);
            when(dtoMapper.toUserDTO(user2)).thenReturn(userDTO2);

            // When
            List<UserDTO> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(userDTO1, userDTO2);
            verify(userRepository).findAll();
            verify(dtoMapper).toUserDTO(user1);
            verify(dtoMapper).toUserDTO(user2);
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsersExist() {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<UserDTO> result = userService.getAllUsers();

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user DTO when user exists")
        void shouldReturnUserDTOWhenUserExists() {
            // Given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(dtoMapper.toUserDTO(testUser)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.getUserById(userId);

            // Then
            assertThat(result).isEqualTo(testUserDTO);
            verify(userRepository).findById(userId);
            verify(dtoMapper).toUserDTO(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Given
            User newUser = TestDataFactory.createTestUser(2L, "new@example.com", "Jane", "Smith");
            UserDTO newUserDTO = new UserDTO(2L, "new@example.com", "Jane", "Smith", false);

            when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("bcryptHash");
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(dtoMapper.toUserDTO(newUser)).thenReturn(newUserDTO);

            // When
            UserDTO result = userService.createUser(createUserRequest);

            // Then
            assertThat(result).isEqualTo(newUserDTO);
            verify(userRepository).existsByEmail(createUserRequest.getEmail());
            verify(passwordEncoder).encode(createUserRequest.getPassword());
            verify(userRepository).save(argThat(user ->
                    user.getEmail().equals(createUserRequest.getEmail()) &&
                            user.getFirstName().equals(createUserRequest.getFirstName()) &&
                            user.getLastName().equals(createUserRequest.getLastName()) &&
                            user.isActive() &&
                            user.isFirstTimeLogin() &&
                            user.getPasswordHashMethod().equals("SHA256_BCRYPT")
            ));
            verify(auditLogService).logAction(eq(newUser), eq("USER_CREATED"), eq("User"), eq(newUser.getId()), contains("User created by admin"));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createUserRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists");

            verify(userRepository, never()).save(any());
            verify(auditLogService, never()).logAction(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should set correct default values for new user")
        void shouldSetCorrectDefaultValuesForNewUser() {
            // Given
            when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("bcryptHash");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(dtoMapper.toUserDTO(any())).thenReturn(testUserDTO);

            // When
            userService.createUser(createUserRequest);

            // Then
            verify(userRepository).save(argThat(user ->
                    user.isActive() &&
                            user.isFirstTimeLogin() &&
                            user.getPasswordHashMethod().equals("SHA256_BCRYPT") &&
                            user.getPassword().equals("bcryptHash")
            ));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully with all fields")
        void shouldUpdateUserSuccessfullyWithAllFields() {
            // Given
            UpdateUserRequest fullUpdateRequest = new UpdateUserRequest();
            fullUpdateRequest.setEmail("updated@example.com");
            fullUpdateRequest.setFirstName("Updated");
            fullUpdateRequest.setLastName("User");
            fullUpdateRequest.setActive(false);

            User updatedUser = TestDataFactory.createTestUser();
            updatedUser.setEmail("updated@example.com");
            updatedUser.setFirstName("Updated");
            updatedUser.setLastName("User");
            updatedUser.setActive(false);

            UserDTO updatedUserDTO = new UserDTO(1L, "updated@example.com", "Updated", "User", false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(updatedUser);
            when(dtoMapper.toUserDTO(updatedUser)).thenReturn(updatedUserDTO);

            // When
            UserDTO result = userService.updateUser(1L, fullUpdateRequest);

            // Then
            assertThat(result).isEqualTo(updatedUserDTO);
            assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
            assertThat(testUser.getFirstName()).isEqualTo("Updated");
            assertThat(testUser.getLastName()).isEqualTo("User");
            assertThat(testUser.isActive()).isFalse();

            verify(auditLogService).logAction(eq(updatedUser), eq("USER_UPDATED"), eq("User"), eq(updatedUser.getId()), contains("User profile updated"));
        }

        @Test
        @DisplayName("Should update only specified fields")
        void shouldUpdateOnlySpecifiedFields() {
            // Given
            UpdateUserRequest partialUpdateRequest = new UpdateUserRequest();
            partialUpdateRequest.setFirstName("NewFirstName");
            // lastName, email, and active are null

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(dtoMapper.toUserDTO(testUser)).thenReturn(testUserDTO);

            String originalEmail = testUser.getEmail();
            String originalLastName = testUser.getLastName();
            boolean originalActive = testUser.isActive();

            // When
            userService.updateUser(1L, partialUpdateRequest);

            // Then
            assertThat(testUser.getFirstName()).isEqualTo("NewFirstName");
            assertThat(testUser.getEmail()).isEqualTo(originalEmail); // unchanged
            assertThat(testUser.getLastName()).isEqualTo(originalLastName); // unchanged
            assertThat(testUser.isActive()).isEqualTo(originalActive); // unchanged
        }

        @Test
        @DisplayName("Should throw exception when user not found for update")
        void shouldThrowExceptionWhenUserNotFoundForUpdate() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(999L, updateUserRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 999");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {
            // Given
            UpdateUserRequest emailUpdateRequest = new UpdateUserRequest();
            emailUpdateRequest.setEmail("existing@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(1L, emailUpdateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow updating to same email")
        void shouldAllowUpdatingToSameEmail() {
            // Given
            UpdateUserRequest sameEmailRequest = new UpdateUserRequest();
            sameEmailRequest.setEmail(testUser.getEmail()); // same email
            sameEmailRequest.setFirstName("Updated");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(dtoMapper.toUserDTO(testUser)).thenReturn(testUserDTO);

            // When
            userService.updateUser(1L, sameEmailRequest);

            // Then
            verify(userRepository, never()).existsByEmail(testUser.getEmail());
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user and related data")
        void shouldDeleteUserAndRelatedData() {
            // Given
            Long userId = 1L;

            // When
            userService.deleteUser(userId);

            // Then
            verify(shiftAssignmentRepository).deleteByUserId(userId);
            verify(userRoleRepository).deleteByUserId(userId);
            verify(auditLogRepository).deleteByUserId(userId);
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should call deletions in correct order")
        void shouldCallDeletionsInCorrectOrder() {
            // Given
            Long userId = 1L;

            // When
            userService.deleteUser(userId);

            // Then
            var inOrder = inOrder(shiftAssignmentRepository, userRoleRepository, auditLogRepository, userRepository);
            inOrder.verify(shiftAssignmentRepository).deleteByUserId(userId);
            inOrder.verify(userRoleRepository).deleteByUserId(userId);
            inOrder.verify(auditLogRepository).deleteByUserId(userId);
            inOrder.verify(userRepository).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("Is Admin Tests")
    class IsAdminTests {

        @Test
        @DisplayName("Should return true when user is admin")
        void shouldReturnTrueWhenUserIsAdmin() {
            // Given
            Long userId = 1L;
            when(userRoleRepository.isUserAdmin(userId)).thenReturn(true);

            // When
            boolean result = userService.isAdmin(userId);

            // Then
            assertThat(result).isTrue();
            verify(userRoleRepository).isUserAdmin(userId);
        }

        @Test
        @DisplayName("Should return false when user is not admin")
        void shouldReturnFalseWhenUserIsNotAdmin() {
            // Given
            Long userId = 1L;
            when(userRoleRepository.isUserAdmin(userId)).thenReturn(false);

            // When
            boolean result = userService.isAdmin(userId);

            // Then
            assertThat(result).isFalse();
            verify(userRoleRepository).isUserAdmin(userId);
        }
    }
}
