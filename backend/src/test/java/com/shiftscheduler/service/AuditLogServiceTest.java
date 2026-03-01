package com.shiftscheduler.service;

import com.shiftscheduler.model.AuditLog;
import com.shiftscheduler.model.User;
import com.shiftscheduler.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Unit Tests")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private User testUser;
    private AuditLog testAuditLog;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setUser(testUser);
        testAuditLog.setAction("CREATE_USER");
        testAuditLog.setEntity("User");
        testAuditLog.setEntityId(1L);
        testAuditLog.setDescription("User created successfully");
        testAuditLog.setIpAddress("192.168.1.1");
        testAuditLog.setActionAt(Instant.now());

        testPageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Log Action Tests")
    class LogActionTests {

        @Test
        @DisplayName("Should successfully log action with IP address from X-Forwarded-For header")
        void shouldLogActionWithXForwardedForHeader() {
            // Given
            String expectedIp = "10.0.0.1";
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class);

            when(mockAttributes.getRequest()).thenReturn(mockRequest);
            when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(expectedIp + ",192.168.1.1");

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(mockAttributes);

                auditLogService.logAction(testUser, "CREATE_USER", "User", 1L, "User created successfully");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getUser()).isEqualTo(testUser);
            assertThat(savedAuditLog.getAction()).isEqualTo("CREATE_USER");
            assertThat(savedAuditLog.getEntity()).isEqualTo("User");
            assertThat(savedAuditLog.getEntityId()).isEqualTo(1L);
            assertThat(savedAuditLog.getDescription()).isEqualTo("User created successfully");
            assertThat(savedAuditLog.getIpAddress()).isEqualTo(expectedIp);
        }

        @Test
        @DisplayName("Should log action with IP address from RemoteAddr when X-Forwarded-For is empty")
        void shouldLogActionWithRemoteAddrWhenXForwardedForEmpty() {
            // Given
            String expectedIp = "192.168.1.100";
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class);

            when(mockAttributes.getRequest()).thenReturn(mockRequest);
            when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("");
            when(mockRequest.getRemoteAddr()).thenReturn(expectedIp);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(mockAttributes);

                auditLogService.logAction(testUser, "UPDATE_USER", "User", 2L, "User updated");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getIpAddress()).isEqualTo(expectedIp);
        }

        @Test
        @DisplayName("Should log action with Unknown IP when no request context available")
        void shouldLogActionWithUnknownIpWhenNoRequestContext() {
            // Given
            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(null);

                auditLogService.logAction(testUser, "DELETE_USER", "User", 3L, "User deleted");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getIpAddress()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should handle exception during IP address retrieval")
        void shouldHandleExceptionDuringIpAddressRetrieval() {
            // Given
            ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class);
            when(mockAttributes.getRequest()).thenThrow(new RuntimeException("Request error"));

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(mockAttributes);

                auditLogService.logAction(testUser, "ERROR_ACTION", "User", 4L, "Error occurred");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getIpAddress()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should log action with null entity ID")
        void shouldLogActionWithNullEntityId() {
            // Given
            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(null);

                auditLogService.logAction(testUser, "SYSTEM_ACTION", "System", null, "System action performed");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getUser()).isEqualTo(testUser);
            assertThat(savedAuditLog.getAction()).isEqualTo("SYSTEM_ACTION");
            assertThat(savedAuditLog.getEntity()).isEqualTo("System");
            assertThat(savedAuditLog.getEntityId()).isNull();
            assertThat(savedAuditLog.getDescription()).isEqualTo("System action performed");
        }

        @Test
        @DisplayName("Should handle X-Forwarded-For header with multiple IPs")
        void shouldHandleXForwardedForWithMultipleIps() {
            // Given
            String multipleIps = "203.0.113.1,198.51.100.1,192.168.1.1";
            String expectedFirstIp = "203.0.113.1";

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class);

            when(mockAttributes.getRequest()).thenReturn(mockRequest);
            when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(multipleIps);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(mockAttributes);

                auditLogService.logAction(testUser, "LOGIN", "User", 1L, "User logged in");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getIpAddress()).isEqualTo(expectedFirstIp);
        }
    }

    @Nested
    @DisplayName("Get Audit Logs by User Tests")
    class GetAuditLogsByUserTests {

        @Test
        @DisplayName("Should return audit logs for specific user")
        void shouldReturnAuditLogsForSpecificUser() {
            // Given
            Long userId = 1L;
            List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
            Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, testPageable, 1);

            when(auditLogRepository.findByUserId(userId, testPageable)).thenReturn(expectedPage);

            // When
            Page<AuditLog> result = auditLogService.getAuditLogsByUser(userId, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testAuditLog);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(auditLogRepository).findByUserId(userId, testPageable);
        }

        @Test
        @DisplayName("Should return empty page when no audit logs found for user")
        void shouldReturnEmptyPageWhenNoAuditLogsFoundForUser() {
            // Given
            Long userId = 999L;
            Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

            when(auditLogRepository.findByUserId(userId, testPageable)).thenReturn(emptyPage);

            // When
            Page<AuditLog> result = auditLogService.getAuditLogsByUser(userId, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(auditLogRepository).findByUserId(userId, testPageable);
        }

        @Test
        @DisplayName("Should handle pagination correctly for user audit logs")
        void shouldHandlePaginationCorrectlyForUserAuditLogs() {
            // Given
            Long userId = 1L;
            Pageable secondPage = PageRequest.of(1, 5);
            List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
            Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, secondPage, 10);

            when(auditLogRepository.findByUserId(userId, secondPage)).thenReturn(expectedPage);

            // When
            Page<AuditLog> result = auditLogService.getAuditLogsByUser(userId, secondPage);

            // Then
            assertThat(result.getNumber()).isEqualTo(1); // Page number
            assertThat(result.getSize()).isEqualTo(5);   // Page size
            assertThat(result.getTotalElements()).isEqualTo(10);

            verify(auditLogRepository).findByUserId(userId, secondPage);
        }
    }

    @Nested
    @DisplayName("Get Audit Logs by Action Tests")
    class GetAuditLogsByActionTests {

        @Test
        @DisplayName("Should return audit logs for specific action")
        void shouldReturnAuditLogsForSpecificAction() {
            // Given
            String action = "CREATE_USER";
            List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
            Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, testPageable, 1);

            when(auditLogRepository.findByAction(action, testPageable)).thenReturn(expectedPage);

            // When
            Page<AuditLog> result = auditLogService.getAuditLogsByAction(action, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testAuditLog);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(auditLogRepository).findByAction(action, testPageable);
        }

        @Test
        @DisplayName("Should return empty page when no audit logs found for action")
        void shouldReturnEmptyPageWhenNoAuditLogsFoundForAction() {
            // Given
            String action = "NONEXISTENT_ACTION";
            Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

            when(auditLogRepository.findByAction(action, testPageable)).thenReturn(emptyPage);

            // When
            Page<AuditLog> result = auditLogService.getAuditLogsByAction(action, testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(auditLogRepository).findByAction(action, testPageable);
        }

        @Test
        @DisplayName("Should handle case-sensitive action search")
        void shouldHandleCaseSensitiveActionSearch() {
            // Given
            String upperCaseAction = "CREATE_USER";
            String lowerCaseAction = "create_user";

            Page<AuditLog> upperCaseResults = new PageImpl<>(Arrays.asList(testAuditLog), testPageable, 1);
            Page<AuditLog> lowerCaseResults = new PageImpl<>(Collections.emptyList(), testPageable, 0);

            when(auditLogRepository.findByAction(upperCaseAction, testPageable)).thenReturn(upperCaseResults);
            when(auditLogRepository.findByAction(lowerCaseAction, testPageable)).thenReturn(lowerCaseResults);

            // When
            Page<AuditLog> upperResult = auditLogService.getAuditLogsByAction(upperCaseAction, testPageable);
            Page<AuditLog> lowerResult = auditLogService.getAuditLogsByAction(lowerCaseAction, testPageable);

            // Then
            assertThat(upperResult.getContent()).hasSize(1);
            assertThat(lowerResult.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get All Audit Logs Tests")
    class GetAllAuditLogsTests {

        @Test
        @DisplayName("Should return all audit logs with pagination")
        void shouldReturnAllAuditLogsWithPagination() {
            // Given
            AuditLog secondLog = new AuditLog();
            secondLog.setId(2L);
            secondLog.setUser(testUser);
            secondLog.setAction("UPDATE_USER");
            secondLog.setEntity("User");
            secondLog.setEntityId(2L);

            List<AuditLog> auditLogs = Arrays.asList(testAuditLog, secondLog);
            Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, testPageable, 2);

            when(auditLogRepository.findAll(testPageable)).thenReturn(expectedPage);

            // When
            Page<AuditLog> result = auditLogService.getAllAuditLogs(testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).containsExactly(testAuditLog, secondLog);

            verify(auditLogRepository).findAll(testPageable);
        }

        @Test
        @DisplayName("Should return empty page when no audit logs exist")
        void shouldReturnEmptyPageWhenNoAuditLogsExist() {
            // Given
            Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

            when(auditLogRepository.findAll(testPageable)).thenReturn(emptyPage);

            // When
            Page<AuditLog> result = auditLogService.getAllAuditLogs(testPageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(auditLogRepository).findAll(testPageable);
        }

        @Test
        @DisplayName("Should handle different page sizes correctly")
        void shouldHandleDifferentPageSizesCorrectly() {
            // Given
            Pageable largePage = PageRequest.of(0, 50);
            List<AuditLog> auditLogs = Collections.nCopies(50, testAuditLog);
            Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, largePage, 100);

            when(auditLogRepository.findAll(largePage)).thenReturn(expectedPage);

            // When
            Page<AuditLog> result = auditLogService.getAllAuditLogs(largePage);

            // Then
            assertThat(result.getSize()).isEqualTo(50);
            assertThat(result.getContent()).hasSize(50);
            assertThat(result.getTotalElements()).isEqualTo(100);

            verify(auditLogRepository).findAll(largePage);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null user gracefully in logAction")
        void shouldHandleNullUserInLogAction() {
            // Given
            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(null);

                auditLogService.logAction(null, "SYSTEM_ACTION", "System", 1L, "System action");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getUser()).isNull();
            assertThat(savedAuditLog.getAction()).isEqualTo("SYSTEM_ACTION");
        }

        @Test
        @DisplayName("Should handle empty strings in logAction")
        void shouldHandleEmptyStringsInLogAction() {
            // Given
            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

            // When
            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(null);

                auditLogService.logAction(testUser, "", "", null, "");
            }

            // Then
            verify(auditLogRepository).save(auditLogCaptor.capture());
            AuditLog savedAuditLog = auditLogCaptor.getValue();

            assertThat(savedAuditLog.getAction()).isEmpty();
            assertThat(savedAuditLog.getEntity()).isEmpty();
            assertThat(savedAuditLog.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("Should verify exact method calls for all operations")
        void shouldVerifyExactMethodCalls() {
            // Given
            Long userId = 1L;
            String action = "TEST_ACTION";

            try (MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(null);

                when(auditLogRepository.findByUserId(userId, testPageable))
                        .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(auditLogRepository.findByAction(action, testPageable))
                        .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(auditLogRepository.findAll(testPageable))
                        .thenReturn(new PageImpl<>(Collections.emptyList()));

                // When
                auditLogService.logAction(testUser, action, "Entity", 1L, "Description");
                auditLogService.getAuditLogsByUser(userId, testPageable);
                auditLogService.getAuditLogsByAction(action, testPageable);
                auditLogService.getAllAuditLogs(testPageable);

                // Then
                verify(auditLogRepository, times(1)).save(any(AuditLog.class));
                verify(auditLogRepository, times(1)).findByUserId(userId, testPageable);
                verify(auditLogRepository, times(1)).findByAction(action, testPageable);
                verify(auditLogRepository, times(1)).findAll(testPageable);
                verifyNoMoreInteractions(auditLogRepository);
            }
        }
    }
}
