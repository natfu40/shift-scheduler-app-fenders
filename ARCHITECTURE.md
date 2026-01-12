# Shift Scheduler Application - Architecture & Implementation Summary

## Overview

The Shift Scheduler is a full-stack web application built with Java Spring Boot (backend) and React (frontend) that enables employees to sign up for shifts while allowing administrators to create and manage shifts, along with comprehensive audit logging of all user actions.

## Architecture

### Backend Architecture (Java Spring Boot)

```
┌─────────────────┐
│   Controllers   │  (REST API endpoints)
└────────┬────────┘
         │
┌────────▼────────┐
│    Services     │  (Business logic)
└────────┬────────┘
         │
┌────────▼────────┐
│ Repositories    │  (Data access)
└────────┬────────┘
         │
┌────────▼────────┐
│   Database      │  (MariaDB)
└─────────────────┘
```

**Security Flow:**
```
Request → JWT Filter → Extract Token → Validate Token → Load User → Execute Endpoint → Response
```

### Frontend Architecture (React)

```
App (Router)
├── Authentication (Login/Signup)
├── EmployeeDashboard (View & Signup for Shifts)
├── AdminDashboard (Create Shifts, Manage Signups, View Audit Logs)
└── Navigation (Header)
```

**State Management:**
- Zustand for authentication state
- React hooks for local component state
- Axios for API calls with JWT token injection

## Key Components

### Backend

#### Models
- **User**: Employee/Admin accounts with email, password, name
- **Role**: User roles (EMPLOYEE, ADMIN)
- **UserRole**: Many-to-many relationship between users and roles
- **Shift**: Available work shifts with date/time and capacity
- **ShiftAssignment**: Tracks which users signed up for which shifts
- **AuditLog**: Complete audit trail of all user actions

#### Services
- **AuthService**: Handles user registration and login
- **ShiftService**: CRUD operations for shifts
- **ShiftAssignmentService**: Manages shift signups
- **AuditLogService**: Logs all user actions

#### Security
- **JwtTokenProvider**: Generates and validates JWT tokens
- **UserPrincipal**: Represents authenticated user
- **CustomUserDetailsService**: Loads user details from database
- **SecurityConfig**: Spring Security configuration with CORS and JWT filters
- **JwtAuthenticationFilter**: Filters requests and validates JWT tokens

### Frontend

#### Pages
- **LoginPage**: User login
- **SignupPage**: User registration
- **EmployeeDashboard**: View available shifts and signup
- **AdminDashboard**: Manage shifts and view audit logs

#### Components
- **Navigation**: Header with logout functionality

#### State Management
- **authStore**: Zustand store for authentication state

#### API Client
- **client.js**: Centralized API calls with axios interceptors for JWT token injection

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles mapping
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Shifts table
CREATE TABLE shifts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    available_slots INT NOT NULL,
    filled_slots INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Shift assignments table
CREATE TABLE shift_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shift_id BIGINT NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    signed_up_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shift_id) REFERENCES shifts(id)
);

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity VARCHAR(255) NOT NULL,
    entity_id BIGINT,
    description VARCHAR(1000),
    ip_address VARCHAR(45),
    action_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Shifts Management
- `GET /api/shifts/available` - List available shifts (public)
- `GET /api/shifts/upcoming` - List upcoming shifts (public)
- `GET /api/shifts/{id}` - Get shift details (public)
- `POST /api/shifts` - Create shift (admin only)
- `PUT /api/shifts/{id}` - Update shift (admin only)
- `DELETE /api/shifts/{id}` - Delete shift (admin only)

### Shift Assignments
- `POST /api/shift-assignments/signup/{shiftId}` - Sign up for shift (authenticated)
- `GET /api/shift-assignments/user` - Get user's assignments (authenticated)
- `GET /api/shift-assignments/shift/{shiftId}` - Get shift signups (admin only)
- `PUT /api/shift-assignments/{id}/accept` - Accept signup (admin only)
- `DELETE /api/shift-assignments/{id}/reject` - Reject signup (admin only)

### Audit Logs
- `GET /api/audit-logs` - Get all logs (admin only)
- `GET /api/audit-logs/user/{userId}` - Get user logs (admin only)
- `GET /api/audit-logs/action/{action}` - Get logs by action (admin only)

## Authentication & Authorization

### JWT Authentication Flow
1. User signs up or logs in
2. Backend validates credentials and generates JWT token
3. Frontend stores token in localStorage
4. Frontend includes token in Authorization header for subsequent requests
5. Backend validates token on each request via JwtAuthenticationFilter

### Role-Based Access Control
- **Public endpoints**: Auth, shift listings
- **User endpoints**: Signup for shifts, view own assignments
- **Admin endpoints**: Create/edit shifts, view all signups, view audit logs

## Security Features

1. **Password Hashing**: BCrypt for secure password storage
2. **JWT Tokens**: Stateless, scalable authentication
3. **CORS Configuration**: Restricted cross-origin access
4. **Input Validation**: Server-side validation of all requests
5. **Audit Logging**: Complete tracking of user actions with IP addresses
6. **Role-Based Access Control**: Endpoint-level authorization

## Deployment

### Docker Deployment
Application includes Docker configuration for containerized deployment:
- `backend/Dockerfile`: Multi-stage build for backend
- `frontend/Dockerfile`: Node.js container for frontend
- `docker-compose.yml`: Orchestrates all services

### Cloud Deployment Options
1. **AWS**: EC2/Beanstalk (backend), RDS (database), S3/CloudFront (frontend)
2. **DigitalOcean**: Droplets (backend), Managed Database (database), App Platform (frontend)
3. **Azure**: App Service (backend), Database for MariaDB, Static Web Apps (frontend)

### Environment Configuration
- Development: `application.properties`
- Production: `application-prod.properties` with environment variables

## Development Workflow

### Backend Development
1. Make changes to Java code
2. Maven automatically compiles and tests
3. Spring Boot DevTools enables hot reload
4. API endpoints are immediately available

### Frontend Development
1. Make changes to React components
2. Hot module reloading automatically refreshes browser
3. State is preserved for faster iteration
4. API calls use axios interceptors to inject JWT token

## Monitoring & Logging

### Audit Logging
- All user actions are logged to `audit_logs` table
- Includes: user, action type, entity, timestamp, IP address
- Accessible via admin audit logs endpoint

### Application Logging
- Backend: Configurable via application.properties
- Frontend: Browser console for development, error tracking in production

## Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
```bash
cd frontend
npm test
```

## Future Enhancements

1. **Real-time Notifications**: WebSockets for instant shift updates
2. **Email Integration**: Send notifications to users
3. **Shift Swapping**: Allow employees to swap shifts
4. **Advanced Filtering**: Search and filter shifts
5. **Calendar View**: Visual shift calendar
6. **User Preferences**: Customizable notifications and settings
7. **Performance Analytics**: Shift coverage reports

## Key Technologies

| Layer | Technology | Version |
|-------|-----------|---------|
| **Backend** | Java | 21+ |
| **Backend Framework** | Spring Boot | 3.2.1 |
| **Authentication** | Spring Security + JWT | 0.12.3 |
| **Database** | MariaDB | 10.5+ |
| **ORM** | Hibernate/JPA | 6.x |
| **Frontend** | React | 18.2 |
| **HTTP Client** | Axios | 1.6.5 |
| **State Management** | Zustand | 4.4.4 |
| **CSS Framework** | Bootstrap | 5.3.2 |
| **Containerization** | Docker | Latest |
| **Orchestration** | Docker Compose | Latest |

## File Structure Summary

```
shift-scheduler/
├── backend/
│   ├── src/main/java/com/shiftscheduler/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── store/
│   │   ├── App.js
│   │   └── index.js
│   ├── public/
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
├── README.md
├── SETUP.md
├── DEVELOPMENT.md
├── ARCHITECTURE.md (this file)
└── .github/workflows/
    └── ci-cd.yml
```

## Conclusion

The Shift Scheduler application provides a complete, production-ready solution for managing employee schedules with comprehensive audit logging and role-based access control. The modular architecture allows for easy extension and maintenance, while the containerized deployment approach ensures consistency across environments.

