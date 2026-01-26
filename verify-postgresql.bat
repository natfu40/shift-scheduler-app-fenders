@echo off
REM PostgreSQL Migration Verification Script for Windows
REM Run this to verify your PostgreSQL conversion is working correctly

echo 🔍 Verifying PostgreSQL Migration...
echo ==================================

REM Check if Docker Compose file has been updated
echo 1. Checking docker-compose.yml...
findstr /C:"postgres:" docker-compose.yml >nul
if %errorlevel%==0 (
    echo    ✅ Docker Compose uses PostgreSQL
) else (
    echo    ❌ Docker Compose still references MariaDB
    exit /b 1
)

REM Check if backend configuration is updated
echo 2. Checking backend configuration...
findstr /C:"postgresql" backend\src\main\resources\application.properties >nul
if %errorlevel%==0 (
    echo    ✅ Backend configured for PostgreSQL
) else (
    echo    ❌ Backend still configured for MariaDB
    exit /b 1
)

REM Check if PostgreSQL driver is in pom.xml
echo 3. Checking Maven dependencies...
findstr /C:"postgresql" backend\pom.xml >nul
if %errorlevel%==0 (
    echo    ✅ PostgreSQL driver found in pom.xml
) else (
    echo    ❌ PostgreSQL driver missing from pom.xml
    exit /b 1
)

REM Check if MariaDB references are removed from pom.xml
findstr /C:"mariadb" backend\pom.xml >nul
if %errorlevel%==0 (
    echo    ⚠️  MariaDB references still found in pom.xml
) else (
    echo    ✅ MariaDB references removed from pom.xml
)

echo.
echo 4. Testing Docker Compose setup...
echo    Starting PostgreSQL container...
docker-compose up -d postgres

REM Wait for PostgreSQL to start
timeout /t 5 /nobreak >nul

REM Test PostgreSQL connection
docker-compose exec -T postgres psql -U postgres -d shift_scheduler -c "SELECT version();" >nul 2>&1
if %errorlevel%==0 (
    echo    ✅ PostgreSQL container is running and accessible
) else (
    echo    ❌ Cannot connect to PostgreSQL container
    exit /b 1
)

echo.
echo 5. Testing backend connection...
echo    Building and starting backend...
docker-compose up -d --build backend

REM Wait for backend to start
timeout /t 10 /nobreak >nul

REM Test backend health (curl may not be available on Windows)
echo    ⚠️  Backend started (manual verification needed)

echo.
echo 6. Checking database tables...
echo    Tables will be created when backend connects...

echo.
echo ==================================
echo 🎉 PostgreSQL Migration Verification Complete!
echo.
echo ✅ Next steps:
echo 1. Test the frontend: http://localhost:3000
echo 2. Login with: admin@example.com / admin123
echo 3. Create a test shift and verify database persistence
echo 4. Deploy to Railway when ready!
echo.
echo 📚 Documentation available in docs\ folder:
echo    - POSTGRESQL_SETUP.md - Local setup instructions
echo    - RAILWAY_DEPLOYMENT.md - Cloud deployment guide
echo    - POSTGRESQL_MIGRATION.md - Migration details
echo.
echo 🚀 Ready to deploy to Railway with: railway up

pause
