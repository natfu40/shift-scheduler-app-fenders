# 🗃️ PostgreSQL Setup Guide

Since the shift scheduler now uses PostgreSQL everywhere (local development and production), you'll need PostgreSQL installed on your development machine.

## 🐘 PostgreSQL Installation

### Windows Installation

#### Option 1: PostgreSQL Official Installer (Recommended)
1. **Download PostgreSQL**
   - Go to https://www.postgresql.org/download/windows/
   - Download PostgreSQL 15 (latest stable version)
   - Run the installer as Administrator

2. **Installation Settings**
   - **Password**: Set to `postgres` (for consistency with docker-compose)
   - **Port**: Keep default `5432`
   - **Locale**: Keep default
   - **Components**: Install all components

3. **Verify Installation**
   ```cmd
   psql --version
   ```

#### Option 2: Chocolatey (if you have it)
```cmd
choco install postgresql
```

#### Option 3: Use Docker Only
If you prefer not to install PostgreSQL directly, you can use only Docker:
```cmd
# Start only PostgreSQL from docker-compose
docker-compose up -d postgres
```

### macOS Installation

#### Option 1: Homebrew (Recommended)
```bash
# Install PostgreSQL
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Create default database
createdb shift_scheduler
```

#### Option 2: Postgres.app
1. Download from https://postgresapp.com/
2. Install and run the app
3. PostgreSQL will be available on port 5432

### Linux (Ubuntu/Debian) Installation

```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create database and user
sudo -u postgres psql
CREATE DATABASE shift_scheduler;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE shift_scheduler TO postgres;
\q
```

## 🔧 Database Setup

### Create Database (if not auto-created)
```sql
-- Connect to PostgreSQL
psql -U postgres -h localhost

-- Create the database
CREATE DATABASE shift_scheduler;

-- Verify it was created
\l

-- Connect to the database
\c shift_scheduler

-- Exit
\q
```

### Connection Test
```bash
# Test connection with credentials from application.properties
psql -U postgres -h localhost -d shift_scheduler -p 5432
```

If successful, you'll see:
```
shift_scheduler=#
```

## 🐳 Docker Alternative (Easiest)

If you prefer to use Docker for local development (recommended):

```bash
# Use the existing docker-compose setup (now with PostgreSQL)
docker-compose up -d

# This will automatically:
# - Start PostgreSQL on localhost:5432
# - Create the shift_scheduler database
# - Set up user: postgres, password: postgres
```

## 🔍 Verification

### 1. Check PostgreSQL is Running
```bash
# Windows (Command Prompt)
sc query postgresql-x64-15

# macOS/Linux
brew services list | grep postgresql
# or
systemctl status postgresql
```

### 2. Test Database Connection
```bash
psql -U postgres -h localhost -d shift_scheduler -c "SELECT version();"
```

### 3. Start Your Application
```bash
# Using Docker Compose (easiest)
docker-compose up -d

# Or manually (if you have PostgreSQL installed locally)
cd backend
mvn spring-boot:run
```

## 🛠️ Common Issues

### Issue: "psql: command not found"
**Solution**: Add PostgreSQL bin directory to your PATH
- **Windows**: Add `C:\Program Files\PostgreSQL\15\bin` to PATH
- **macOS**: `echo 'export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"' >> ~/.zshrc`
- **Linux**: Usually automatically added during installation

### Issue: "password authentication failed"
**Solution**: Reset postgres user password
```sql
sudo -u postgres psql
ALTER USER postgres PASSWORD 'postgres';
```

### Issue: "database shift_scheduler does not exist"
**Solution**: Create the database manually
```sql
psql -U postgres -h localhost
CREATE DATABASE shift_scheduler;
```

### Issue: Connection refused
**Solution**: Start PostgreSQL service
- **Windows**: Start "postgresql-x64-15" service in Services
- **macOS**: `brew services start postgresql@15`
- **Linux**: `sudo systemctl start postgresql`

## 🎯 Quick Reference

### Default Connection Details
```properties
Host: localhost
Port: 5432
Database: shift_scheduler  
Username: postgres
Password: postgres
```

### Useful PostgreSQL Commands
```sql
-- List all databases
\l

-- Connect to database
\c shift_scheduler

-- List all tables
\dt

-- Describe table structure
\d users

-- Exit
\q
```

### Management Tools
- **pgAdmin**: Web-based PostgreSQL administration (optional)
- **DBeaver**: Universal database tool (optional)
- **Command Line**: `psql` (included with PostgreSQL)

---

**Recommendation**: Use Docker Compose for the easiest setup! It handles all the PostgreSQL configuration automatically and matches your production environment exactly.
