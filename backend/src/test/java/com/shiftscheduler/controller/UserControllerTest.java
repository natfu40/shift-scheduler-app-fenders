package com.shiftscheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftscheduler.config.TestAuthenticationConfig;
import com.shiftscheduler.config.TestControllerConfig;
import com.shiftscheduler.config.WebMvcTestSecurityConfig;
import com.shiftscheduler.dto.CreateUserRequest;
import com.shiftscheduler.dto.UpdateUserRequest;
import com.shiftscheduler.dto.UserDTO;
import com.shiftscheduler.exception.ResourceNotFoundException;
import com.shiftscheduler.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({TestControllerConfig.class, WebMvcTestSecurityConfig.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUserDTO;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO(1L, "test@example.com", "John", "Doe", false);
        createUserRequest = new CreateUserRequest("new@example.com", "password", "Jane", "Smith");
        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("Updated");
        updateUserRequest.setLastName("Name");
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get all users when authenticated as admin")
        void shouldGetAllUsersWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            List<UserDTO> users = Arrays.asList(
                    new UserDTO(1L, "user1@example.com", "User", "One", false),
                    new UserDTO(2L, "user2@example.com", "User", "Two", true)
            );
            when(userService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/users")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                    .andExpect(jsonPath("$[0].admin").value(false))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].email").value("user2@example.com"))
                    .andExpect(jsonPath("$[1].admin").value(true));

            verify(userService).getAllUsers();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to get all users")
        void shouldReturnForbiddenWhenNonAdminTriesToGetAllUsers() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(userService, never()).getAllUsers();
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users")
                            .with(csrf()))
                    .andExpect(status().isForbidden()); // Spring Security returns 403 for unauthenticated requests
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get user by ID when authenticated as admin")
        void shouldGetUserByIdWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            when(userService.getUserById(userId)).thenReturn(testUserDTO);

            // When & Then
            mockMvc.perform(get("/api/users/{userId}", userId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));

            verify(userService).getUserById(userId);
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should get own user data when authenticated as same user")
        void shouldGetOwnUserDataWhenAuthenticatedAsSameUser() throws Exception {
            // Given
            Long userId = 1L;
            when(userService.getUserById(userId)).thenReturn(testUserDTO);

            // When & Then
            mockMvc.perform(get("/api/users/{userId}", userId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).getUserById(userId);
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 2L, roles = {"USER"})
        @DisplayName("Should return forbidden when non-admin tries to get other user's data")
        void shouldReturnForbiddenWhenNonAdminTriesToGetOtherUsersData() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/{userId}", 1L)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(userService, never()).getUserById(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return not found when user doesn't exist")
        void shouldReturnNotFoundWhenUserDoesntExist() throws Exception {
            // Given
            Long userId = 999L;
            when(userService.getUserById(userId))
                    .thenThrow(new ResourceNotFoundException("User not found with id: " + userId));

            // When & Then
            mockMvc.perform(get("/api/users/{userId}", userId)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create user when authenticated as admin")
        void shouldCreateUserWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            UserDTO newUserDTO = new UserDTO(2L, "new@example.com", "Jane", "Smith", false);
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(newUserDTO);

            // When & Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.email").value("new@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"));

            verify(userService).createUser(any(CreateUserRequest.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to create user")
        void shouldReturnForbiddenWhenNonAdminTriesToCreateUser() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserRequest)))
                    .andExpect(status().isForbidden());

            verify(userService, never()).createUser(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request when email already exists")
        void shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new IllegalArgumentException("Email already exists"));

            // When & Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update user when authenticated as admin")
        void shouldUpdateUserWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            UserDTO updatedUserDTO = new UserDTO(userId, "test@example.com", "Updated", "Name", false);
            when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedUserDTO);

            // When & Then
            mockMvc.perform(put("/api/users/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"));

            verify(userService).updateUser(eq(userId), any(UpdateUserRequest.class));
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 1L, roles = {"USER"})
        @DisplayName("Should update own user data when authenticated as same user")
        void shouldUpdateOwnUserDataWhenAuthenticatedAsSameUser() throws Exception {
            // Given
            Long userId = 1L;
            UserDTO updatedUserDTO = new UserDTO(userId, "test@example.com", "Updated", "Name", false);
            when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedUserDTO);

            // When & Then
            mockMvc.perform(put("/api/users/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequest)))
                    .andExpect(status().isOk());

            verify(userService).updateUser(eq(userId), any(UpdateUserRequest.class));
        }

        @Test
        @TestAuthenticationConfig.WithMockAppUser(id = 2L, roles = {"USER"})
        @DisplayName("Should return forbidden when non-admin tries to update other user")
        void shouldReturnForbiddenWhenNonAdminTriesToUpdateOtherUser() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/users/{userId}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequest)))
                    .andExpect(status().isForbidden());

            verify(userService, never()).updateUser(any(), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return not found when updating non-existent user")
        void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
            // Given
            Long userId = 999L;
            when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                    .thenThrow(new ResourceNotFoundException("User not found with id: " + userId));

            // When & Then
            mockMvc.perform(put("/api/users/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete user when authenticated as admin")
        void shouldDeleteUserWhenAuthenticatedAsAdmin() throws Exception {
            // Given
            Long userId = 1L;
            doNothing().when(userService).deleteUser(userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(userId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when non-admin tries to delete user")
        void shouldReturnForbiddenWhenNonAdminTriesToDeleteUser() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", 1L)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(userService, never()).deleteUser(any());
        }
    }
}
