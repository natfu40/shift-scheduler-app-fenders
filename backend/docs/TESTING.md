# Test Suite Documentation

## Overview

This document describes the comprehensive test suite for the Shift Scheduler Backend application. The tests are organized in multiple layers following best practices for Spring Boot applications.

## Test Structure

### 1. Unit Tests (`src/test/java/com/shiftscheduler/service/`)

#### AuthServiceTest
- **Purpose**: Tests authentication business logic in isolation
- **Coverage**: 
  - User signup with validation
  - Password-based login with hashed passwords
  - Password change functionality
  - Legacy user migration scenarios
- **Key Features**:
  - Uses Mockito for dependency mocking
  - Tests error conditions (user not found, wrong password, etc.)
  - Validates audit logging
  - Tests both SHA256_BCRYPT and legacy BCRYPT users

#### UserServiceTest  
- **Purpose**: Tests user management business logic
- **Coverage**:
  - CRUD operations for users
  - Email uniqueness validation
  - Admin permission checking
  - Cascade deletion of related data
- **Key Features**:
  - Comprehensive validation testing
  - Tests partial updates
  - Verifies audit trail creation

#### JwtTokenProviderTest
- **Purpose**: Tests JWT token generation and validation
- **Coverage**:
  - Token generation with correct claims
  - Token validation (valid/invalid/expired)
  - User ID extraction from tokens
- **Key Features**:
  - Tests token expiration handling
  - Validates security of token generation

### 2. Repository Integration Tests (`src/test/java/com/shiftscheduler/repository/`)

#### UserRepositoryTest
- **Purpose**: Tests data persistence layer with real database
- **Coverage**:
  - Basic CRUD operations
  - Custom query methods (findByEmail, existsByEmail)
  - Database constraints (unique email)
  - JPA lifecycle callbacks (timestamps)
- **Key Features**:
  - Uses `@DataJpaTest` for optimized database testing
  - Tests database constraint enforcement
  - Validates JPA entity lifecycle

### 3. Controller Integration Tests (`src/test/java/com/shiftscheduler/controller/`)

#### AuthControllerTest
- **Purpose**: Tests REST endpoints for authentication
- **Coverage**:
  - Signup endpoint with validation
  - Login endpoints (both /login and /login-hashed)
  - Password change endpoints
  - Error handling and HTTP status codes
- **Key Features**:
  - Uses `@WebMvcTest` for web layer testing
  - Tests security annotations
  - Validates JSON request/response handling
  - Tests CORS configuration

#### UserControllerTest
- **Purpose**: Tests REST endpoints for user management
- **Coverage**:
  - CRUD endpoints with proper HTTP methods
  - Role-based access control (ADMIN vs USER)
  - Self-access permissions (users can access own data)
  - Error handling and status codes
- **Key Features**:
  - Tests `@PreAuthorize` security expressions
  - Uses `@WithMockUser` for authentication testing
  - Validates role-based authorization

### 4. Full Integration Tests (`src/test/java/com/shiftscheduler/integration/`)

#### AuthenticationIntegrationTest
- **Purpose**: End-to-end testing of authentication flow
- **Coverage**:
  - Complete user registration → database persistence → login flow
  - Database transaction handling
  - Real PostgreSQL database using TestContainers
- **Key Features**:
  - Uses TestContainers for real database testing
  - Tests complete user workflows
  - Validates data persistence across operations
  - Tests error scenarios with real database constraints

## Test Configuration

### Test Profiles
- **application-test.properties**: H2 database for unit tests
- **TestContainers**: PostgreSQL for integration tests

### Test Dependencies
- **Spring Boot Test Starter**: Core testing framework
- **Spring Security Test**: Security testing utilities  
- **TestContainers**: Real database containers for integration testing
- **AssertJ**: Fluent assertions
- **Mockito**: Mocking framework
- **JsonPath**: JSON response testing

### Helper Classes
- **TestDataFactory**: Centralized test data creation
- **TestConfig**: Common test configuration

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Classes
```bash
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=UserRepositoryTest
mvn test -Dtest=AuthenticationIntegrationTest
```

### Run Tests by Category
```bash
# Unit tests only
mvn test -Dtest="**/*Test"

# Integration tests only  
mvn test -Dtest="**/integration/*Test"
```

## Test Coverage

The test suite provides comprehensive coverage across:

1. **Business Logic**: Service layer unit tests with mocked dependencies
2. **Data Access**: Repository tests with real database operations
3. **Web Layer**: Controller tests with security and JSON handling
4. **Integration**: End-to-end workflows with full application context
5. **Security**: Authentication, authorization, and JWT testing

## Best Practices Demonstrated

1. **Test Isolation**: Each test is independent and can run in any order
2. **Descriptive Names**: Test methods clearly describe what is being tested
3. **AAA Pattern**: Arrange-Act-Assert structure in all tests
4. **Proper Mocking**: Dependencies are mocked at appropriate levels
5. **Real Database Testing**: Integration tests use actual database
6. **Security Testing**: Authentication and authorization are thoroughly tested
7. **Error Scenarios**: Both happy path and error conditions are tested

## Future Test Enhancements

1. **Performance Tests**: Add load testing for critical endpoints
2. **Contract Tests**: Add consumer-driven contract tests
3. **Mutation Testing**: Validate test quality with mutation testing
4. **Test Data Builders**: Enhance TestDataFactory with builder pattern
5. **Parameterized Tests**: Add more data-driven test scenarios
