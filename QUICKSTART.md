# Shift Scheduler - Quick Reference Guide

## Getting Started (5 minutes)

### Option 1: Docker (Recommended)
```bash
# Clone the repo (if not already done)
cd shift-scheduler

# Start all services
docker-compose up -d

# Access the app
# Frontend: http://localhost:3000
# Backend: http://localhost:8080/api
# Database: localhost:3306 (mysql root/root)
```

### Option 2: Manual Setup

**Terminal 1 - Database:**
```bash
# Make sure MariaDB is running
mysql -u root -p
CREATE DATABASE shift_scheduler;
```

**Terminal 2 - Backend:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
# Runs on http://localhost:8080/api
```

**Terminal 3 - Frontend:**
```bash
cd frontend
npm install
npm start
# Runs on http://localhost:3000
```

## User Registration & Testing

### Create Test Accounts

1. **Employee Account**
   - Go to http://localhost:3000/signup
   - Fill in details
   - Click "Sign Up"
   - You'll be logged in automatically

2. **To Access Admin Features**
   - You'll need to manually grant admin role via database:
   ```sql
   -- First, create ADMIN and EMPLOYEE roles if they don't exist
   INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator');
   INSERT INTO roles (name, description) VALUES ('EMPLOYEE', 'Employee');
   
   -- Get the user ID and admin role ID
   SELECT id FROM users WHERE email = 'your-email@example.com';
   SELECT id FROM roles WHERE name = 'ADMIN';
   
   -- Assign admin role
   INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
   ```

## Common Commands

### Backend Commands
```bash
cd backend

# Build
mvn clean install

# Run with hot reload
mvn spring-boot:run

# Run tests
mvn test

# Build Docker image
docker build -t shift-scheduler-backend .

# View logs
mvn spring-boot:run -X
```

### Frontend Commands
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Build Docker image
docker build -t shift-scheduler-frontend .
```

### Docker Commands
```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mariadb

# Stop all services
docker-compose down

# Restart services
docker-compose restart

# Rebuild and start
docker-compose up -d --build

# Remove all data (WARNING: deletes database)
docker-compose down -v
```

## API Quick Reference

### Auth
```bash
# Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123","firstName":"John","lastName":"Doe"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123"}'
```

### Shifts (requires auth token in header)
```bash
# Get available shifts
curl http://localhost:8080/api/shifts/available

# Create shift (admin only)
curl -X POST http://localhost:8080/api/shifts \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Morning Shift","startTime":"2024-01-15T08:00:00","endTime":"2024-01-15T16:00:00","availableSlots":5}'

# Sign up for shift
curl -X POST http://localhost:8080/api/shift-assignments/signup/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get user's assignments
curl http://localhost:8080/api/shift-assignments/user \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## File Locations & Key Files

### Backend
| File | Purpose |
|------|---------|
| `backend/pom.xml` | Maven dependencies |
| `backend/src/main/resources/application.properties` | Dev configuration |
| `backend/src/main/resources/application-prod.properties` | Prod configuration |
| `backend/src/main/java/com/shiftscheduler/controller/` | REST endpoints |
| `backend/src/main/java/com/shiftscheduler/service/` | Business logic |
| `backend/src/main/java/com/shiftscheduler/model/` | Database entities |

### Frontend
| File | Purpose |
|------|---------|
| `frontend/package.json` | npm dependencies |
| `frontend/src/App.js` | Main app component |
| `frontend/src/api/client.js` | API calls |
| `frontend/src/store/authStore.js` | Auth state |
| `frontend/src/pages/` | Page components |

## Troubleshooting

### "Connection refused" error
```bash
# Check if MariaDB is running
mysql -u root -p

# On Windows
net start MariaDB

# On Mac
brew services start mariadb

# On Linux
sudo systemctl start mysql
```

### "Port already in use"
```bash
# Find and kill process
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

### Frontend can't connect to backend
- Check backend is running: `curl http://localhost:8080/api/shifts/available`
- Check CORS settings in `SecurityConfig.java`
- Check browser console for errors (F12)

### Database issues
```bash
# Check database exists
mysql -u root -p -e "SHOW DATABASES;"

# Check tables exist
mysql -u root -p shift_scheduler -e "SHOW TABLES;"

# Check user data
mysql -u root -p shift_scheduler -e "SELECT * FROM users;"
```

## Development Tips

### Hot Reload Backend
- Spring Boot DevTools is configured
- Changes to Java files auto-compile
- No need to restart manually

### Hot Reload Frontend
- React hot reload is built-in
- Changes appear in browser instantly
- State is preserved

### Database Debugging
```bash
# Connect to database
mysql -u root -p shift_scheduler

# View tables
SHOW TABLES;

# Check data
SELECT * FROM users;
SELECT * FROM shifts;
SELECT * FROM audit_logs;

# View table structure
DESCRIBE users;
```

## Performance Tips

### Backend
- Pagination is used for list endpoints (page, size parameters)
- Database indexes on foreign keys are automatically created
- Use Postman for API testing (better than curl)

### Frontend
- Bootstrap provides responsive design out of the box
- React.memo prevents unnecessary re-renders
- State management with Zustand is lightweight

## Production Checklist

- [ ] Change JWT secret in `application-prod.properties`
- [ ] Set up SSL/HTTPS
- [ ] Configure database credentials via environment variables
- [ ] Update CORS origins for production domain
- [ ] Set logging level to INFO or WARN
- [ ] Test all endpoints with production data
- [ ] Setup monitoring and alerting
- [ ] Configure database backups
- [ ] Review and update security headers
- [ ] Setup rate limiting

## Useful Documentation Links

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [React Docs](https://react.dev)
- [Bootstrap Docs](https://getbootstrap.com)
- [JWT Explanation](https://jwt.io)
- [MariaDB Docs](https://mariadb.com/kb)

## Getting Help

1. Check the README.md for detailed documentation
2. Review DEVELOPMENT.md for development guidelines
3. Check ARCHITECTURE.md for system design
4. Review logs: `docker-compose logs <service>`
5. Check database directly: `mysql -u root -p shift_scheduler`

## Key Concepts

**JWT Token**: Self-contained authentication token that includes user information
**CORS**: Cross-Origin Resource Sharing - allows frontend and backend to communicate
**DTOs**: Data Transfer Objects - used for API request/response payloads
**Entities**: JPA entities that map to database tables
**Repositories**: Data access objects that interact with database
**Services**: Business logic layer between controllers and repositories
**Controllers**: REST endpoints that handle HTTP requests

## Architecture Overview

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTP/HTTPS
┌──────▼──────┐         ┌──────────────┐
│  React App  │◄───────►│ Spring Boot  │
│  (Frontend) │         │  (Backend)   │
└─────────────┘         └──────┬───────┘
                               │ SQL
                        ┌──────▼──────┐
                        │   MariaDB   │
                        │ (Database)  │
                        └─────────────┘
```

---

**Last Updated**: January 2024
**Version**: 1.0.0

