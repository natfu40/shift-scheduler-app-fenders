# Shift Scheduler - Development Guide

## Project Structure

```
shift-scheduler/
├── backend/
│   ├── src/main/java/com/shiftscheduler/
│   │   ├── config/           # Configuration classes (Security, etc.)
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── model/            # JPA entity models
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # Security utilities (JWT, UserPrincipal, etc.)
│   │   ├── service/          # Business logic services
│   │   └── ShiftSchedulerApplication.java  # Main Spring Boot application
│   ├── src/main/resources/
│   │   └── application.properties  # Application configuration
│   ├── pom.xml              # Maven configuration
│   └── Dockerfile           # Docker configuration
│
├── frontend/
│   ├── src/
│   │   ├── api/             # API client functions
│   │   ├── components/      # React components
│   │   ├── pages/           # Page components
│   │   ├── store/           # Zustand state management
│   │   ├── App.js           # Main App component
│   │   └── index.js         # React entry point
│   ├── public/
│   │   └── index.html       # HTML template
│   ├── package.json         # npm configuration
│   └── Dockerfile           # Docker configuration
│
├── docker-compose.yml       # Docker Compose configuration
├── README.md               # Main documentation
├── DEVELOPMENT.md          # This file
└── .github/
    └── workflows/
        └── ci-cd.yml       # CI/CD pipeline configuration
```

## Development Setup

### Prerequisites
- Java 21
- Node.js 16+
- MariaDB 10.5+
- Maven 3.6+
- Git

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd shift-scheduler
   ```

2. **Setup Database**
   ```sql
   CREATE DATABASE shift_scheduler;
   ```

3. **Start Backend**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

4. **Start Frontend** (in a new terminal)
   ```bash
   cd frontend
   npm install
   npm start
   ```

The application will be available at `http://localhost:3000`

## Code Style & Conventions

### Backend (Java)
- Follow Java naming conventions (camelCase)
- Use meaningful variable names
- Add Javadoc for public methods
- Use Lombok annotations to reduce boilerplate
- Keep services focused on business logic

### Frontend (JavaScript/React)
- Use functional components with hooks
- Keep components small and focused
- Use descriptive naming for components and functions
- Use const/let, avoid var
- Add PropTypes or TypeScript for type safety

## Common Tasks

### Adding a New Feature

1. **Backend**
   - Create model in `model/`
   - Create repository in `repository/`
   - Create service in `service/`
   - Create controller in `controller/`
   - Add endpoints and document in README

2. **Frontend**
   - Create component in `components/` or `pages/`
   - Add API calls in `api/client.js`
   - Update routing if needed
   - Style with Bootstrap classes

### Running Tests

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

### Building for Production

```bash
# Backend
cd backend
mvn clean package

# Frontend
cd frontend
npm run build
```

## Database Migrations

The application uses Hibernate for schema management. Changes are automatically applied on startup based on the `spring.jpa.hibernate.ddl-auto` setting.

For production, set: `spring.jpa.hibernate.ddl-auto=validate`

## Debugging

### Backend
1. Add breakpoints in IDE
2. Run with debugging: `mvn spring-boot:run -Dmaven.surefire.debug`
3. Use Spring Boot DevTools for hot reload

### Frontend
1. Use browser DevTools (F12)
2. Enable Redux DevTools if using Redux
3. Use `console.log()` for debugging
4. Hot reload is built-in with `npm start`

## Common Issues & Solutions

### Port Already in Use
```bash
# Backend (8080)
lsof -i :8080
kill -9 <PID>

# Frontend (3000)
lsof -i :3000
kill -9 <PID>
```

### Database Connection Issues
- Ensure MariaDB is running
- Check credentials in `application.properties`
- Verify database exists

### CORS Errors
- Check frontend URL in SecurityConfig CORS configuration
- Ensure backend is running on correct port

## Git Workflow

1. Create a feature branch: `git checkout -b feature/feature-name`
2. Make changes and commit: `git commit -m "Add feature description"`
3. Push to remote: `git push origin feature/feature-name`
4. Create a Pull Request
5. After review, merge to main

## Performance Optimization Tips

### Backend
- Use pagination for large datasets
- Add database indexes for frequently queried columns
- Cache frequently accessed data
- Use lazy loading for relationships

### Frontend
- Use React.memo for expensive components
- Implement code splitting with React.lazy
- Optimize images and assets
- Use pagination for large lists

## Security Best Practices

- Change JWT secret in production
- Use HTTPS in production
- Implement rate limiting
- Validate all user inputs
- Keep dependencies updated
- Use environment variables for sensitive data

## Useful Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [Bootstrap Documentation](https://getbootstrap.com/docs)
- [JWT Introduction](https://jwt.io/introduction)
- [MariaDB Documentation](https://mariadb.com/documentation/)

## Contact

For questions or issues, please reach out to the development team.

