# Shift Scheduler Backend

Spring Boot backend API for the Shift Scheduler application.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL (or Docker)

### Development Setup

1. **Start PostgreSQL:**
   ```bash
   # Using Docker
   docker-compose up postgres -d
   
   # Or install PostgreSQL locally and create database 'shift_scheduler'
   ```

2. **Run the application:**
   ```bash
   # From root directory
   mvn spring-boot:run
   ```
   
   API will be available at `http://localhost:8080`

## 🐳 Docker Development

### Backend + Database
```bash
# Start backend and PostgreSQL
docker-compose up -d

# Backend API: http://localhost:8080
# Database: localhost:5432
```

### With Frontend (Separate Repository)
```bash
# 1. Clone frontend repository
git clone <frontend-repo-url> ../shift-scheduler-frontend

# 2. Uncomment frontend service in docker-compose.yml

# 3. Start all services
docker-compose up -d
```

## 🚀 Production Deployment

### Railway Deployment
This repository is configured for Railway deployment:

1. **Connect to Railway** from GitHub
2. **Add PostgreSQL service** in Railway dashboard
3. **Set environment variable:** `JWT_SECRET=your-secure-secret`
4. **Deploy automatically** from main branch

### Environment Variables
- `JWT_SECRET` - JWT signing secret (required)
- `DATABASE_URL` - PostgreSQL connection URL (Railway provides)
- `SPRING_PROFILES_ACTIVE` - Active profile (railway/dev/prod)

## 📁 Project Structure

```
shift-scheduler-backend/
├── src/main/java/com/shiftscheduler/
│   ├── controller/          # REST API controllers
│   ├── service/            # Business logic services
│   ├── model/              # JPA entities
│   ├── repository/         # Data access repositories
│   ├── security/           # Security configuration
│   ├── config/             # Application configuration
│   └── dto/                # Data transfer objects
├── src/main/resources/
│   ├── application.properties              # Default configuration
│   ├── application-dev.properties          # Development settings
│   ├── application-prod.properties         # Production settings
│   └── application-railway.properties      # Railway deployment
├── docs/                   # Documentation
├── pom.xml                 # Maven dependencies
├── Dockerfile              # Container configuration
└── railway.json            # Railway deployment settings
```

## 🔧 API Features

### Authentication
- **POST** `/api/auth/login` - User login
- **POST** `/api/auth/signup` - User registration
- **POST** `/api/auth/change-password-hashed` - Change password

### Shift Management
- **GET** `/api/shifts` - List all shifts
- **POST** `/api/shifts` - Create new shift
- **PUT** `/api/shifts/{id}` - Update shift
- **DELETE** `/api/shifts/{id}` - Delete shift

### User Management (Admin)
- **GET** `/api/admin/users` - List users
- **POST** `/api/admin/users` - Create user
- **DELETE** `/api/admin/users/{id}` - Delete user

### Monitoring
- **GET** `/actuator/health` - Health check endpoint

## 🛠 Development

### Available Profiles
- `dev` - Local development with PostgreSQL
- `railway` - Railway deployment configuration
- `prod` - Production settings

### Database
- **ORM**: JPA/Hibernate
- **Database**: PostgreSQL
- **Migrations**: Auto DDL update in development

### Security
- **Authentication**: JWT tokens
- **Password**: bcrypt + SHA256 hashing
- **CORS**: Configurable origins
- **Admin**: Role-based access control

## 🔗 Related Repositories

- **Frontend**: [shift-scheduler-frontend](../shift-scheduler-frontend)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
