package com.shiftscheduler.repository;

import com.shiftscheduler.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("UserRepository Simple Tests")
class UserRepositorySimpleTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setActive(true);
        testUser.setFirstTimeLogin(false);
        testUser.setPasswordHashMethod("BCRYPT");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save and retrieve user by ID")
    void shouldSaveAndRetrieveUserById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("John");
        assertThat(foundUser.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        // Given
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void shouldEnforceUniqueEmailConstraint() {
        // Given
        entityManager.persistAndFlush(testUser);

        User duplicateUser = new User();
        duplicateUser.setEmail("test@example.com"); // Same email
        duplicateUser.setPassword("anotherPassword");
        duplicateUser.setFirstName("Jane");
        duplicateUser.setLastName("Smith");
        duplicateUser.setActive(true);
        duplicateUser.setFirstTimeLogin(false);
        duplicateUser.setPasswordHashMethod("BCRYPT");
        duplicateUser.setCreatedAt(LocalDateTime.now());
        duplicateUser.setUpdatedAt(LocalDateTime.now());

        // When & Then
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(duplicateUser);
        }).satisfiesAnyOf(
            throwable -> assertThat(throwable.getMessage()).containsIgnoringCase("constraint"),
            throwable -> assertThat(throwable.getMessage()).containsIgnoringCase("unique")
        );
    }

    @Test
    @DisplayName("Should handle email case sensitivity correctly")
    void shouldHandleEmailCaseSensitivityCorrectly() {
        // Given
        testUser.setEmail("Test@Example.com");
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByEmail("Test@Example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("Test@Example.com");
    }
}
