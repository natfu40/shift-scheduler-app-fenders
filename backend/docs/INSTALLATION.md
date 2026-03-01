# 🔧 Installation Guide

This guide provides comprehensive instructions for installing, configuring, and deploying the Fenders Brewing Scheduler application.

## 📋 Prerequisites

### System Requirements

#### Development Environment
- **Operating System**: Windows 10+, macOS 10.15+, or Linux (Ubuntu 18.04+)
- **Java**: JDK 21 or higher
- **Node.js**: Version 16 or higher
- **Maven**: Version 3.6 or higher
- **Git**: Latest version
- **Docker**: Latest version (for containerized deployment)

#### Production Environment
- **Database**: PostgreSQL 15+ 
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: 10GB minimum for application and database
- **Network**: HTTPS capability for production deployments

### Required Tools Installation

#### Java 21 Installation
```bash
# On Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# On macOS (using Homebrew)
brew install openjdk@21

# On Windows
# Download from Oracle or use package manager like Chocolatey
choco install openjdk21
```

#### Node.js Installation
```bash
# On Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# On macOS
brew install node

# On Windows
# Download from nodejs.org or use Chocolatey
choco install nodejs
```

#### Maven Installation
```bash
# On Ubuntu/Debian
sudo apt install maven

# On macOS
brew install maven

# On Windows
choco install maven
```

## 🐳 Docker Deployment (Recommended)

The easiest way to deploy the application is using Docker Compose.

### Quick Start with Docker

1. **Clone the Repository**
```bash
git clone <repository-url>
cd shift-scheduler
```

2. **Start the Application**
```bash
docker-compose up -d --build
```

3. **Verify Deployment**
```bash
docker-compose ps
```

4. **Access the Application**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Database: localhost:5432

### Docker Configuration

The `docker-compose.yml` includes:

#### Services
- **PostgreSQL Database** (Port 5432)
  - Database: `shift_scheduler`
  - Username: `postgres`
  - Password: `postgres`

- **Backend Service** (Port 8080)
  - Spring Boot application
  - Automatic database connection
  - JWT authentication configured

- **Frontend Service** (Port 3000)
  - React application
  - Connected to backend API

#### Environment Variables
```yaml
# Backend Environment
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/shift_scheduler
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres
JWT_SECRET: your-secret-key-change-in-production-at-least-32-characters-long!!!

# Frontend Environment  
REACT_APP_API_URL: http://localhost:8080/api
```

### Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs

# Rebuild and start
docker-compose up -d --build

# Check service status
docker-compose ps
```

## 💻 Manual Development Setup

For development or custom deployment scenarios.

### Backend Setup

1. **Clone Repository**
```bash
git clone <repository-url>
cd shift-scheduler/backend
```

2. **Database Setup**
```sql
-- Connect to PostgreSQL as superuser
psql -U postgres -h localhost

-- Create database
CREATE DATABASE shift_scheduler;
CREATE USER scheduler_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE shift_scheduler TO scheduler_user;
-- Grant schema permissions
\c shift_scheduler
GRANT ALL ON SCHEMA public TO scheduler_user;
```

3. **Configure Application Properties**
Edit `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/shift_scheduler
spring.datasource.username=scheduler_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-very-secure-secret-key-at-least-32-characters-long-for-production
jwt.expiration=86400000

# Server Configuration
server.port=8080

# CORS Configuration (for development)
cors.allowed-origins=http://localhost:3000
```

4. **Build and Run Backend**
```bash
# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/shift-scheduler-backend-1.0.0.jar
```

### Frontend Setup

1. **Navigate to Frontend Directory**
```bash
cd shift-scheduler/frontend
```

2. **Install Dependencies**
```bash
npm install
```

3. **Configure Environment**
Create `.env.local`:
```bash
REACT_APP_API_URL=http://localhost:8080/api
```

4. **Start Development Server**
```bash
npm start
```

The frontend will be available at http://localhost:3000

### Database Schema Initialization

The application uses Spring Boot's automatic schema generation. On first startup:

1. **Automatic Table Creation**
   - Users table with authentication fields
   - Roles table (ADMIN, USER)
   - Shifts table with scheduling information
   - Shift assignments table
   - Audit logs table

2. **Default Data Initialization**
   - Default admin user: `admin@example.com` / `admin123`
   - Default roles: ADMIN and USER
   - Sample data (optional)

## 🚀 Production Deployment

### Security Configuration

#### 1. Change Default Credentials
```sql
-- Update default admin password
UPDATE users SET password = '$2a$10$NEW_HASHED_PASSWORD' WHERE email = 'admin@example.com';
```

#### 2. Secure JWT Secret
Generate a secure JWT secret:
```bash
# Generate random 64-character secret
openssl rand -base64 64
```

Update in `application.properties`:
```properties
jwt.secret=YOUR_GENERATED_SECRET_HERE
```

#### 3. Database Security
- Use strong database passwords
- Restrict database access to application server only
- Enable SSL for database connections
- Regular database backups

#### 4. HTTPS Configuration
```properties
# Enable HTTPS
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
```

### Environment-Specific Configuration

#### Production Properties
Create `application-prod.properties`:
```properties
# Database (production)
spring.datasource.url=jdbc:postgresql://prod-db-server:5432/shift_scheduler
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Security
jwt.secret=${JWT_SECRET}

# Logging
logging.level.root=WARN
logging.level.com.shiftscheduler=INFO
logging.file.name=logs/shift-scheduler.log

# JPA (production)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

#### Frontend Production Build
```bash
# Build for production
npm run build

# Deploy build folder to web server
cp -r build/* /var/www/html/
```

### Reverse Proxy Configuration

#### Nginx Configuration
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name your-domain.com;
    
    # SSL Configuration
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    # Frontend
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
    
    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### Apache Configuration
```apache
<VirtualHost *:80>
    ServerName your-domain.com
    Redirect permanent / https://your-domain.com/
</VirtualHost>

<VirtualHost *:443>
    ServerName your-domain.com
    
    DocumentRoot /var/www/html
    
    # SSL Configuration
    SSLEngine on
    SSLCertificateFile /path/to/certificate.crt
    SSLCertificateKeyFile /path/to/private.key
    
    # Frontend
    <Directory /var/www/html>
        RewriteEngine On
        RewriteBase /
        RewriteRule ^index\.html$ - [L]
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteCond %{REQUEST_FILENAME} !-d
        RewriteRule . /index.html [L]
    </Directory>
    
    # Backend API Proxy
    ProxyPreserveHost On
    ProxyPass /api/ http://localhost:8080/api/
    ProxyPassReverse /api/ http://localhost:8080/api/
</VirtualHost>
```

## 🔧 Configuration Options

### Backend Configuration

#### Database Options
```properties
# PostgreSQL (recommended)
spring.datasource.url=jdbc:postgresql://localhost:5432/shift_scheduler
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
```

#### JWT Configuration
```properties
# Token expiration (milliseconds)
jwt.expiration=86400000  # 24 hours
jwt.expiration=3600000   # 1 hour (more secure)

# Issuer and audience
jwt.issuer=shift-scheduler
jwt.audience=employees
```

#### CORS Configuration
```properties
# Development
cors.allowed-origins=http://localhost:3000

# Production (multiple origins)
cors.allowed-origins=https://yourdomain.com,https://app.yourdomain.com

# Allow all (not recommended for production)
cors.allowed-origins=*
```

### Frontend Configuration

#### Environment Variables
```bash
# API Configuration
REACT_APP_API_URL=http://localhost:8080/api

# Feature Flags
REACT_APP_ENABLE_CALENDAR=true
REACT_APP_ENABLE_NOTIFICATIONS=true

# Debugging
REACT_APP_DEBUG_MODE=false
```

#### Build Configuration
```json
// package.json
{
  "homepage": "/scheduler",
  "scripts": {
    "build": "react-scripts build",
    "build:prod": "NODE_ENV=production react-scripts build"
  }
}
```

## 📊 Monitoring and Logging

### Application Monitoring

#### Health Checks
```bash
# Backend health check
curl http://localhost:8080/actuator/health

# Database connectivity
curl http://localhost:8080/actuator/health/db
```

#### Log Configuration
```properties
# Logging levels
logging.level.root=INFO
logging.level.com.shiftscheduler=DEBUG
logging.level.org.springframework.security=DEBUG

# Log file
logging.file.name=logs/shift-scheduler.log
logging.file.max-size=10MB
logging.file.max-history=30

# Log pattern
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### Database Monitoring
```sql
-- Check database size
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'shift_scheduler'
GROUP BY table_schema;

-- Monitor active connections
SHOW PROCESSLIST;

-- Check table sizes
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'shift_scheduler'
ORDER BY (data_length + index_length) DESC;
```

## 🛠️ Maintenance Tasks

### Regular Maintenance

#### Daily Tasks
- Monitor application logs for errors
- Check database connectivity
- Verify backup completion
- Monitor disk space usage

#### Weekly Tasks
- Review audit logs
- Archive old shift data
- Check for system updates
- Performance monitoring

#### Monthly Tasks
- Database optimization
- Security patch updates
- Backup validation
- Capacity planning review

### Database Maintenance
```sql
-- Optimize tables
OPTIMIZE TABLE users, roles, shifts, shift_assignments, audit_logs;

-- Archive old audit logs (older than 1 year)
DELETE FROM audit_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Archive old shifts (older than 6 months)
DELETE FROM shift_assignments WHERE shift_id IN (
    SELECT id FROM shifts WHERE end_time < DATE_SUB(NOW(), INTERVAL 6 MONTH)
);
DELETE FROM shifts WHERE end_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```

### Backup Procedures
```bash
#!/bin/bash
# Database backup script
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/shift-scheduler"
DB_NAME="shift_scheduler"

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup
pg_dump -U postgres -h localhost $DB_NAME > $BACKUP_DIR/db_backup_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/db_backup_$DATE.sql

# Remove backups older than 30 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

# Application files backup
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz /path/to/application
```

## 🚨 Troubleshooting

### Common Issues

#### Backend Won't Start
```bash
# Check Java version
java -version

# Check port availability
netstat -tulpn | grep :8080

# Check database connectivity
psql -U scheduler_user -h localhost -d shift_scheduler

# View application logs
tail -f logs/shift-scheduler.log
```

#### Frontend Build Issues
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Check Node.js version
node --version
npm --version
```

#### Database Connection Issues
```bash
# Test database connection
psql -U scheduler_user -h localhost -d shift_scheduler

# Check database service
systemctl status postgresql

# Verify database configuration
cat src/main/resources/application.properties | grep datasource
```

#### Docker Issues
```bash
# Check Docker status
docker --version
docker-compose --version

# View container logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres

# Restart services
docker-compose down
docker-compose up -d --build
```

### Performance Issues

#### Slow Database Queries
```sql
-- Enable query logging (PostgreSQL)
ALTER SYSTEM SET log_min_duration_statement = 2000;  -- 2 seconds
SELECT pg_reload_conf();

-- Check slow queries (requires logging configured)
-- View PostgreSQL logs for slow queries

-- Add indexes for performance
CREATE INDEX idx_shifts_start_time ON shifts(start_time);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

#### High Memory Usage
```bash
# Check Java heap size
java -XX:+PrintFlagsFinal -version | grep HeapSize

# Adjust JVM settings
export JAVA_OPTS="-Xmx2g -Xms1g"
java $JAVA_OPTS -jar shift-scheduler-backend.jar
```

## 📞 Support and Updates

### Getting Help
1. **Documentation**: Check this installation guide and user guides
2. **Logs**: Review application and database logs for error details
3. **Community**: Check repository issues and discussions
4. **Professional Support**: Contact system administrators for enterprise support

### Updating the Application
```bash
# Backup current installation
./backup.sh

# Pull latest code
git pull origin main

# Backend updates
cd backend
mvn clean install
systemctl restart shift-scheduler-backend

# Frontend updates  
cd ../frontend
npm install
npm run build
cp -r build/* /var/www/html/

# Database migrations (if any)
# Check migration scripts in src/main/resources/db/migration/
```

### Version Control
- **Stable Branch**: `main` - Production-ready releases
- **Development Branch**: `develop` - Latest features (may be unstable)
- **Release Tags**: Use tagged versions for production deployments

---

*For additional support, refer to the repository documentation or contact your system administrator.*

*Last updated: January 26, 2026*
