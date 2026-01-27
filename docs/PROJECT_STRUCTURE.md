# Project Structure Guide

## Overview
This document explains the organization of the Shift Scheduler Backend repository.

## Directory Structure

```
shift-scheduler-backend/
├── src/                    # Application source code
├── deployment/             # Deployment configurations
├── scripts/                # Build and start scripts
├── docs/                   # Documentation
├── pom.xml                 # Maven project configuration
├── railway.json            # Railway deployment config
├── nixpacks.toml           # Nixpacks build config
└── README.md               # Main documentation
```

## Directory Details

### `/src/`
Contains all Java source code and application resources.

**Structure:**
- `src/main/java/com/shiftscheduler/` - Java source code
  - `controller/` - REST API endpoints
  - `service/` - Business logic
  - `model/` - JPA entities
  - `repository/` - Data access layer
  - `security/` - Authentication & authorization
  - `config/` - Application configuration
  - `dto/` - Data transfer objects
  - `util/` - Utility classes
- `src/main/resources/` - Configuration files
  - `application.properties` - Default settings
  - `application-dev.properties` - Development settings
  - `application-prod.properties` - Production settings
  - `application-railway.properties` - Railway deployment settings

### `/deployment/`
All deployment-related configuration files.

**Files:**
- `docker-compose.yml` - Development Docker setup (backend + PostgreSQL)
- `docker-compose.prod.yml` - Production Docker setup
- `Dockerfile` - Container image definition

**Usage:**
```bash
# Development
docker-compose -f deployment/docker-compose.yml up

# Production
docker-compose -f deployment/docker-compose.prod.yml up -d
```

### `/scripts/`
Build and startup scripts for various environments.

**Files:**
- `build.sh` - General Maven build script
- `start.sh` - General application startup script
- `railway-build.sh` - Railway-specific build script
- `railway-start.sh` - Railway-specific startup script

**Usage:**
```bash
# Build the application
./scripts/build.sh

# Start the application
./scripts/start.sh
```

### `/docs/`
Comprehensive documentation for the project.

**Files:**
- `README.md` - Documentation index
- `INSTALLATION.md` - Installation and setup guide
- `USER_GUIDE.md` - End-user documentation
- `ADMIN_GUIDE.md` - Administrator guide
- `RAILWAY_DEPLOYMENT.md` - Railway deployment instructions
- `PRODUCTION_README.md` - Production deployment guide
- `PROJECT_STRUCTURE.md` - This file (repository organization)

## Root Files

### Essential Configuration
- `pom.xml` - Maven project configuration and dependencies
- `railway.json` - Railway deployment configuration (must be in root)
- `nixpacks.toml` - Nixpacks build configuration (must be in root)

### Documentation
- `README.md` - Main project documentation and quick start guide

### Version Control
- `.gitignore` - Git ignore patterns
- `.railwayignore` - Railway deployment exclusions

### IDE
- `.idea/` - IntelliJ IDEA configuration (gitignored)

## Why This Structure?

### Benefits
1. **Clarity** - Related files are grouped together
2. **Scalability** - Easy to add new deployment configs or scripts
3. **Separation of Concerns** - Code, deployment, and docs are separate
4. **Industry Standard** - Follows common Java/Spring Boot patterns
5. **Clean Root** - Only essential files in root directory

### Deployment Considerations
- Railway configuration files (`railway.json`, `nixpacks.toml`) must stay in root
- Docker files are in `deployment/` but referenced from root in configs
- Scripts can be in subdirectory as they're not auto-executed

## Common Tasks

### Local Development
```bash
# Start database
docker-compose -f deployment/docker-compose.yml up postgres -d

# Run application
mvn spring-boot:run
```

### Building
```bash
# Use Maven directly
mvn clean package

# Or use build script
./scripts/build.sh
```

### Deployment
```bash
# Docker development
docker-compose -f deployment/docker-compose.yml up

# Docker production
docker-compose -f deployment/docker-compose.prod.yml up -d

# Railway (automatic on git push)
git push origin main
```

## Adding New Files

### New Deployment Config
Add to `deployment/` directory

### New Build Script
Add to `scripts/` directory and make executable:
```bash
chmod +x scripts/your-script.sh
```

### New Documentation
Add to `docs/` directory and link from `docs/README.md`

## References
- Main README: `../README.md`
- Installation Guide: `INSTALLATION.md`
- Railway Deployment: `RAILWAY_DEPLOYMENT.md`
- Production Deployment: `PRODUCTION_README.md`
