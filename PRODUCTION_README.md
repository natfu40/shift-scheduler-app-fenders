# Shift Scheduler - Production Deployment Guide

## Overview
This is a production-ready shift scheduling application with the following components:
- **Backend**: Spring Boot REST API with PostgreSQL
- **Frontend**: React SPA with Bootstrap UI
- **Authentication**: JWT-based authentication with password change requirements
- **Database**: PostgreSQL with audit logging
- **Deployment**: Docker Compose with production optimizations

## Production Deployment

### Prerequisites
- Docker and Docker Compose
- SSL certificates (for HTTPS)
- Strong JWT secret key
- Secure database credentials

### Step 1: Environment Configuration

1. Copy the environment template:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your production values:
   ```bash
   # Database Configuration
   DB_USERNAME=your_production_db_user
   DB_PASSWORD=your_very_secure_password
   DB_NAME=shift_scheduler
   
   # JWT Configuration (CRITICAL: Generate a strong secret)
   JWT_SECRET=your-cryptographically-secure-jwt-secret-at-least-256-bits
   
   # Domain Configuration
   REACT_APP_API_URL=https://yourdomain.com
   ```

### Step 2: SSL Configuration (Recommended)

1. Place SSL certificates in `./nginx/ssl/`:
   ```
   ./nginx/ssl/certificate.crt
   ./nginx/ssl/private.key
   ```

2. Update nginx configuration for HTTPS in `docker-compose.prod.yml`

### Step 3: Deploy

```bash
# Production deployment
docker-compose -f docker-compose.prod.yml up -d

# Monitor logs
docker-compose -f docker-compose.prod.yml logs -f

# Health check
curl https://yourdomain.com/health
```

### Step 4: Initial Setup

1. The application will create a default admin user:
   - **Email**: admin@example.com
   - **Password**: admin123

2. **IMPORTANT**: Change the admin password immediately after first login.

## Development Setup

```bash
# Development environment
docker-compose up -d

# View logs
docker-compose logs -f backend frontend
```

## Database Backup

```bash
# Backup
docker-compose exec postgres pg_dump -U postgres shift_scheduler > backup.sql

# Restore
docker-compose exec -T postgres psql -U postgres shift_scheduler < backup.sql
```

## Security Considerations

### ✅ Production Security Features
- Non-root container users
- Environment variable configuration
- Security headers (CSP, HSTS, etc.)
- Input validation and sanitization
- JWT token expiration
- Password hashing (bcrypt + SHA256)
- SQL injection protection (JPA)
- CORS configuration
- Audit logging

### 🔒 Additional Security Recommendations
1. **Use HTTPS only** in production
2. **Generate strong JWT secret** (256+ bits)
3. **Regular database backups**
4. **Monitor application logs**
5. **Update dependencies regularly**
6. **Use a reverse proxy** (nginx included)
7. **Implement rate limiting**
8. **Use a web application firewall**

## Monitoring and Maintenance

### Health Check Endpoints
- Frontend: `GET /health`
- Backend: `GET /actuator/health`

### Log Locations
- Application logs: Docker container logs
- Nginx access logs: `/var/log/nginx/access.log`
- Database logs: PostgreSQL container logs

### Regular Maintenance
1. **Update Docker images** monthly
2. **Backup database** daily
3. **Monitor disk space** and logs
4. **Review audit logs** for suspicious activity
5. **Update SSL certificates** before expiration

## Application Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (Admin/User)
- Mandatory password change on first login
- Secure password storage (bcrypt + SHA256)

### Shift Management
- Create, edit, and delete shifts
- Employee shift signup
- Admin approval workflow
- Calendar integration (iCal export)

### Audit & Compliance
- Complete audit logging
- User activity tracking
- Admin action monitoring

### User Interface
- Responsive Bootstrap design
- Real-time updates
- Calendar views
- Mobile-friendly interface

## Troubleshooting

### Common Issues

1. **Database connection errors**
   - Check PostgreSQL container status
   - Verify database credentials
   - Ensure network connectivity

2. **JWT authentication failures**
   - Verify JWT_SECRET is set correctly
   - Check token expiration settings
   - Clear browser storage

3. **Frontend not loading**
   - Check nginx configuration
   - Verify REACT_APP_API_URL is correct
   - Check CORS settings

### Debug Commands

```bash
# Check container status
docker-compose ps

# View specific service logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres

# Execute commands in containers
docker-compose exec backend bash
docker-compose exec postgres psql -U postgres shift_scheduler
```

## Performance Optimization

### Database
- Connection pooling configured
- Proper indexing on frequently queried fields
- Query optimization with JPA

### Application
- JVM tuning for containers
- G1 garbage collector
- Container-aware memory settings

### Frontend
- Static asset caching
- Gzip compression
- Minified JavaScript/CSS

## License & Support

This is a production-ready application. For support:
1. Check application logs
2. Review this documentation
3. Check Docker container health
4. Monitor database performance

**Security Notice**: Always use HTTPS in production and keep all dependencies up to date.
