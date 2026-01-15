# Fenders Brewing Scheduler Application
This project is licensed under the MIT License.

## License

For issues or questions, please create an issue in the repository.

## Support

- Verify JWT token in localStorage
- Check CORS configuration
- Ensure backend is running on port 8080
### Frontend API errors

- Verify Java 17 is installed
- Check database connection properties
- Ensure MariaDB is running
### Backend won't start

## Troubleshooting

- Performance metrics and reports
- User preferences and settings
- Calendar view for shifts
- Advanced filtering and search
- Shift swap functionality
- Email notifications for shift assignments
- Real-time notifications for new shifts

## Future Enhancements

- **Audit Logging**: Complete tracking of user actions
- **Role-Based Access Control**: Endpoint-level authorization
- **CORS Configuration**: Controlled access from frontend
- **Password Hashing**: BCrypt for password security
- **JWT Authentication**: Stateless, scalable authentication

## Security Features

- Monitor user activities
- View all audit logs
- Accept/reject employee signups
- Edit and delete shifts
- Create new shifts
### Admin

- Track their action history
- View their own assignments
- Sign up for shifts
- View available shifts
### Employee

## User Roles

   - Frontend: Static Web Apps
   - Database: Azure Database for MariaDB
   - Backend: App Service
3. **Azure**:

   - Frontend: App Platform
   - Database: Managed Database for MariaDB
   - Backend: Droplets
2. **DigitalOcean**:

   - Frontend: S3 + CloudFront
   - Database: RDS for MariaDB
   - Backend: EC2 or Elastic Beanstalk
1. **AWS**:

### Cloud Deployment Options

```
docker run -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/shift_scheduler -p 8080:8080 shift-scheduler-backend
docker build -t shift-scheduler-backend .
```bash

Build and run:

```
ENTRYPOINT ["java", "-jar", "app.jar"]
COPY target/shift-scheduler-backend-1.0.0.jar app.jar
FROM amazoncorretto:21
```dockerfile

Create a `Dockerfile` for the backend:

### Using Docker

## Deployment

6. **audit_logs** - Audit trail of all user actions
5. **shift_assignments** - User signups for shifts
4. **shifts** - Available shifts
3. **user_roles** - Mapping between users and roles
2. **roles** - User roles (EMPLOYEE, ADMIN)
1. **users** - User account information
### Tables

## Database Schema

- `GET /api/audit-logs/action/{action}` - Get logs for a specific action (Admin only)
- `GET /api/audit-logs/user/{userId}` - Get logs for a specific user (Admin only)
- `GET /api/audit-logs` - Get all audit logs (Admin only)
### Audit Logs

- `DELETE /api/shift-assignments/{assignmentId}/reject` - Reject a signup (Admin only)
- `PUT /api/shift-assignments/{assignmentId}/accept` - Accept a signup (Admin only)
- `GET /api/shift-assignments/shift/{shiftId}` - Get signups for a shift (Admin only)
- `GET /api/shift-assignments/user` - Get user's shift assignments
- `POST /api/shift-assignments/signup/{shiftId}` - Sign up for a shift
### Shift Assignments

- `DELETE /api/shifts/{shiftId}` - Delete a shift (Admin only)
- `PUT /api/shifts/{shiftId}` - Update a shift (Admin only)
- `POST /api/shifts` - Create a shift (Admin only)
- `GET /api/shifts/{shiftId}` - Get shift details
- `GET /api/shifts/upcoming` - Get upcoming shifts (paginated)
- `GET /api/shifts/available` - Get available shifts (paginated)
### Shifts

- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/signup` - Register a new user
### Authentication

## API Endpoints

**Important**: Change `jwt.secret` to a secure, random string in production.

```
jwt.expiration=86400000
jwt.secret=your-secret-key-change-in-production-at-least-32-characters-long!!!
# JWT

spring.datasource.password=root
spring.datasource.username=root
spring.datasource.url=jdbc:mariadb://localhost:3306/shift_scheduler
# Database
```properties

### Backend Configuration (application.properties)

## Configuration

The frontend will start on `http://localhost:3000`

```
npm start
# Start the development server

npm install
# Install dependencies

cd frontend
```bash

### 3. Frontend Setup

The backend will start on `http://localhost:8080`

```
mvn spring-boot:run
# Run the application

mvn clean install
# Build the project

cd backend
```bash

### 2. Backend Setup

```
CREATE DATABASE shift_scheduler;
```sql

Create a MariaDB database:

### 1. Database Setup

## Installation & Setup

- npm 8+
- Node.js 16+
### Frontend

- MariaDB 10.5+ or MySQL 8.0+
- Maven 3.6+
- Java 17 or higher
### Backend

## Prerequisites

- **Date-fns** for date formatting
- **Bootstrap 5** for styling
- **Zustand** for state management
- **Axios** for API calls
- **React Router** for navigation
- **React 18** for UI components
### Frontend

- **MariaDB** (or PostgreSQL alternative) for data persistence
- **Spring Data JPA** for database operations
- **JWT** for token-based authentication
- **Spring Security** for authentication and authorization
- **Java 17** with Spring Boot 3.2.1
### Backend

## Technology Stack

- **Responsive UI**: Built with React and Bootstrap
- **Role-Based Access Control**: Different features for employees and administrators
- **Audit Logging**: Track all user actions (signup, login, shift creation, etc.)
- **Admin Dashboard**: Create and manage shifts, view signups, and access audit logs
- **Employee Dashboard**: View available shifts and sign up for desired shifts
- **User Authentication**: Sign up and login with JWT-based authentication

## Features

A full-stack web application that helps employees pick their work schedules and allows administrators to manage shifts and track user actions.


