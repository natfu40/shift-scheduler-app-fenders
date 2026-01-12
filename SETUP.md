# Shift Scheduler - Setup Instructions

## Quick Start with Docker

The easiest way to get the application running is with Docker Compose:

```bash
# Make sure you have Docker and Docker Compose installed
docker-compose up -d
```

This will start:
- MariaDB on port 3306
- Backend on port 8080
- Frontend on port 3000

Access the application at `http://localhost:3000`

## Manual Setup

### Prerequisites
- Java 21+
- Node.js 16+
- MariaDB 10.5+
- Maven 3.6+

### Database Setup

1. **Install MariaDB** (if not already installed)
   - Windows: Download from https://mariadb.org/download/
   - macOS: `brew install mariadb`
   - Linux: `apt-get install mariadb-server`

2. **Create Database**
   ```bash
   mysql -u root -p
   
   CREATE DATABASE shift_scheduler;
   ```

3. **Configure Backend** (optional)
   Edit `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### Backend Setup

```bash
cd backend

# Build
mvn clean install

# Run
mvn spring-boot:run
```

Backend will be available at `http://localhost:8080/api`

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run
npm start
```

Frontend will be available at `http://localhost:3000`

## Default Login

After the databases are initialized, you can create a new account via the signup page, or if you want to seed test data:

```bash
# This would be done via the API or by manually inserting test data
```

## Troubleshooting

### Issue: Backend won't start - "Connection refused"
**Solution**: Make sure MariaDB is running
```bash
# On Windows (if installed via MSI)
net start MariaDB

# On macOS
brew services start mariadb

# On Linux
sudo systemctl start mysql
```

### Issue: Port already in use
```bash
# Kill process using the port (example: 8080)
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :8080
kill -9 <PID>
```

### Issue: Frontend can't connect to backend
- Check that backend is running on port 8080
- Verify CORS is enabled (should be in SecurityConfig)
- Check browser console for errors

## Next Steps

1. Review the [README.md](README.md) for detailed documentation
2. Check [DEVELOPMENT.md](DEVELOPMENT.md) for development guidelines
3. Explore the API endpoints documentation in README.md
4. Start customizing the application for your needs

## Support

For issues or questions, please check the documentation or create an issue in the repository.

