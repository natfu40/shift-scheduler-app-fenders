# 🚂 Railway Deployment Guide for Shift Scheduler

## Overview

Railway **fully supports** your shift scheduler application! The app now uses **PostgreSQL everywhere** for consistency and better performance:

- **Local Development**: PostgreSQL via Docker Compose
- **Railway Production**: PostgreSQL (fully managed by Railway)

This unified database approach means:
- ✅ **Same database** in development and production
- ✅ **No migration issues** between environments
- ✅ **Consistent behavior** across all deployments
- ✅ **Better testing** - your local environment matches production exactly

## Why PostgreSQL Everywhere is Better

### Advantages of PostgreSQL:
- ✅ **Better JSON support** (for future features)
- ✅ **Superior concurrency** handling
- ✅ **More advanced indexing** capabilities
- ✅ **Better performance** for complex queries
- ✅ **Fully managed** by Railway in production (automatic backups, updates, monitoring)
- ✅ **Consistent experience** between local development and production
- ✅ **No environment differences** - what works locally works in production

### Unified Database Benefits:
Your application now uses PostgreSQL consistently because:
- **Same database engine** in all environments
- **Identical SQL behavior** locally and in production
- **No surprises** when deploying to Railway
- **Better testing reliability** - local tests match production behavior exactly

## 🚀 Deployment Steps

### 1. Install Railway CLI

```bash
# Windows (using npm)
npm install -g @railway/cli

# Or using PowerShell
iwr -useb https://railway.app/install.ps1 | iex

# Verify installation
railway version
```

### 2. Login to Railway

```bash
railway login
```
This will open your browser for authentication.

### 3. Initialize Your Project

```bash
cd c:\Users\nate\Documents\github\shift-scheduler
railway init
```

Choose:
- **"Empty Project"** when prompted
- **Name**: "shift-scheduler" or your preferred name

### 4. Add PostgreSQL Database

```bash
railway add postgresql
```

This automatically:
- ✅ Provisions a PostgreSQL database
- ✅ Sets up DATABASE_URL environment variable  
- ✅ Configures automatic backups
- ✅ Enables connection pooling

### 5. Deploy Backend Service

```bash
cd backend
railway up
```

Railway automatically detects:
- ✅ Your Dockerfile
- ✅ Java/Spring Boot application
- ✅ Builds and deploys your backend
- ✅ Sets up health checks

### 6. Configure Environment Variables

```bash
# Set JWT secret (generate a secure one)
railway variables set JWT_SECRET=your-very-secure-64-character-secret-key-for-production-use-only

# Verify variables are set
railway variables
```

### 7. Deploy Frontend Service

```bash
cd ../frontend
railway service add frontend
railway up
```

### 8. Link Services

Railway automatically handles service communication, but you may need to update the frontend API URL:

```bash
# Get your backend URL
railway status

# Set frontend environment variable
railway variables set REACT_APP_API_URL=https://your-backend-url.railway.app/api
```

## 🔧 Configuration Changes Made

### Backend Changes:
1. **Converted to PostgreSQL**: Updated pom.xml, application.properties, and docker-compose.yml
2. **Unified database configuration**: Same PostgreSQL setup for local and production
3. **Updated Dockerfile** to use Railway profile when needed
4. **Added railway.json** for deployment configuration

### Database Configuration:
```properties
# Unified PostgreSQL configuration (works locally and on Railway)
spring.datasource.url=jdbc:postgresql://localhost:5432/shift_scheduler  # Local
spring.datasource.url=${DATABASE_URL}  # Railway overrides this automatically
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Environment Variables Railway Provides:
- `DATABASE_URL`: Complete PostgreSQL connection string
- `PORT`: Assigned port (Railway handles this)
- `RAILWAY_ENVIRONMENT`: Deployment environment

## 📊 Cost Estimation

### Railway Pricing for Your App:
```
Free Tier ($5/month credit):
✅ Backend service: ~$3-4/month
✅ PostgreSQL database: ~$1-2/month  
✅ Frontend service: ~$0-1/month
Total: Within free tier for development!

Production Scaling:
- Backend: $5-15/month (based on usage)
- Database: $2-8/month (based on storage/connections)
- Frontend: $1-3/month
Total: $8-26/month for production
```

## 🎯 Deployment Commands Summary

```bash
# Complete deployment in ~5 minutes
cd c:\Users\nate\Documents\github\shift-scheduler

# 1. Setup
railway login
railway init

# 2. Add database
railway add postgresql

# 3. Deploy backend
cd backend
railway up

# 4. Set environment variables
railway variables set JWT_SECRET=your-secure-secret-here

# 5. Deploy frontend
cd ../frontend  
railway service add frontend
railway variables set REACT_APP_API_URL=https://your-backend-url.railway.app/api
railway up

# 6. Your app is live!
railway open
```

## ✅ Verification Steps

### 1. Check Backend Health
```bash
# Get backend URL
railway status

# Test API endpoint
curl https://your-backend-url.railway.app/api/health
```

### 2. Verify Database Connection
Railway dashboard shows:
- ✅ Database connection status
- ✅ Connection pool metrics  
- ✅ Query performance stats

### 3. Test Frontend
```bash
railway open frontend
```

### 4. Test Full Application Flow
1. ✅ Login with default admin credentials
2. ✅ Create a test shift
3. ✅ Sign up for the shift
4. ✅ Verify database persistence

## 🔄 Local Development vs Railway

### Local Development:
```bash
# Now uses PostgreSQL locally too!
docker-compose up -d
# Access at http://localhost:3000
# Database: PostgreSQL on localhost:5432
```

### Railway Production:
```bash
# Same PostgreSQL, fully managed by Railway
railway open
# Access at https://your-app.railway.app
```

## 🛠️ Database Migration (if needed)

If you have existing MariaDB data you want to migrate:

```bash
# 1. Export from MariaDB
mysqldump -u root -p shift_scheduler > backup.sql

# 2. Convert to PostgreSQL format (if needed)
# Most basic SQL will work directly

# 3. Import to Railway PostgreSQL
railway connect postgresql
\i backup.sql
```

## 🎉 Advantages of This Setup

### ✅ **Zero Configuration**: Railway handles everything automatically
### ✅ **Unified Database**: PostgreSQL everywhere - no environment differences
### ✅ **Automatic Scaling**: Railway scales based on demand
### ✅ **Built-in Monitoring**: Real-time metrics and logging
### ✅ **Automatic HTTPS**: SSL certificates managed automatically
### ✅ **Git Integration**: Deploy on every push (optional)
### ✅ **Consistent Testing**: Local environment matches production exactly

## 🚨 Important Notes

### Database Consistency:
- **Same PostgreSQL version** in development and production
- **Identical behavior** across all environments
- **No migration surprises** when deploying

### First-Time Deployment:
1. **Default admin user** will be created automatically
2. **Database schema** will be created via Hibernate
3. **Login**: `admin@example.com` / `admin123` (change immediately!)

---

**Result**: Your application now uses PostgreSQL consistently everywhere, giving you better performance, managed infrastructure, and perfect development/production parity!

Ready to deploy? Run the deployment commands above and your shift scheduler will be live in minutes! 🚀
