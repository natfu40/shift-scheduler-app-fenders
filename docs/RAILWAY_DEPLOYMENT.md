# Railway Deployment Guide for Shift Scheduler

## Overview
This guide covers deploying the Shift Scheduler application to Railway.app, a modern deployment platform that simplifies application hosting.

## Prerequisites
- Railway account (https://railway.app)
- GitHub repository with your code
- Railway CLI (optional but recommended)

## Backend Deployment on Railway

### Step 1: Create New Project
1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Choose your shift-scheduler repository
5. Railway will auto-detect the backend service

### Step 2: Configure Environment Variables
In your Railway project dashboard, add these environment variables:

**Required Variables:**
```
JWT_SECRET=your-very-secure-jwt-secret-at-least-256-bits-long
```

**Optional Variables (Railway provides defaults):**
```
PORT=8080                    # Railway sets this automatically
DATABASE_URL=...             # Railway provides this when you add PostgreSQL
JWT_EXPIRATION=86400000      # 24 hours in milliseconds
```

### Step 3: Add PostgreSQL Database
1. In your Railway project, click "New Service"
2. Select "PostgreSQL"
3. Railway will automatically provide the `DATABASE_URL` environment variable

### Step 4: Configure Deployment Settings
The `railway.json` file in the backend directory contains:
- Dockerfile-based build configuration
- Health check endpoint (`/actuator/health`)
- Restart policy settings

### Step 5: Deploy
1. Push your code to GitHub
2. Railway will automatically build and deploy
3. Monitor deployment in Railway dashboard

## Frontend Deployment Options

### Option 1: Static Site Deployment (Recommended)
Deploy frontend as a static site on services like:
- **Vercel** (recommended for React)
- **Netlify**  
- **Railway Static Sites**

#### Environment Variables for Frontend:
```
REACT_APP_API_URL=https://your-backend-domain.railway.app
```

### Option 2: Deploy Frontend on Railway
If you want to deploy frontend on Railway too:

1. Create another Railway service for frontend
2. Use the production Dockerfile: `Dockerfile.prod`
3. Set environment variable: `REACT_APP_API_URL`

## Post-Deployment Configuration

### 1. Custom Domain (Optional)
- In Railway dashboard, go to Settings > Networking
- Add your custom domain
- Update `REACT_APP_API_URL` to use your custom domain

### 2. Environment-Specific Settings
Railway automatically uses the `railway` Spring profile, which:
- Uses PostgreSQL database provided by Railway
- Optimizes connection pooling for Railway's infrastructure
- Enables production logging levels
- Includes actuator health checks

### 3. Initial Admin Setup
After deployment:
1. Visit your Railway app URL
2. Login with default admin credentials:
   - Email: `admin@example.com`
   - Password: `admin123`
3. **Important**: Change the admin password immediately

## Security Considerations for Railway

### ✅ Configured Security Features:
- Environment variable-based configuration
- Non-root container execution
- Secure JWT token handling
- Production logging configuration
- Database connection security

### 🔒 Additional Security Steps:
1. **Generate Strong JWT Secret**:
   ```bash
   # Generate a secure 256-bit key
   openssl rand -base64 32
   ```

2. **Use Custom Domain with HTTPS**:
   - Railway provides HTTPS by default
   - Configure your custom domain in Railway dashboard

3. **Monitor Application**:
   - Use Railway's built-in monitoring
   - Check `/actuator/health` endpoint regularly

## Monitoring and Maintenance

### Health Checks
Railway will monitor your application health via:
- **Health Endpoint**: `/actuator/health`
- **Application Logs**: Available in Railway dashboard
- **Metrics**: CPU, memory, and request metrics

### Database Management
- **Backups**: Railway provides automatic PostgreSQL backups
- **Scaling**: Can upgrade database plan as needed
- **Monitoring**: Database metrics available in dashboard

### Application Logs
View logs in Railway dashboard or use Railway CLI:
```bash
railway logs --follow
```

## Cost Optimization

### Railway Pricing Tiers:
- **Hobby Plan**: Free tier with resource limits
- **Pro Plan**: Pay-per-use with higher limits
- **Team/Enterprise**: Advanced features and support

### Optimization Tips:
1. **Right-size your resources**: Monitor usage and adjust
2. **Database optimization**: Use connection pooling (already configured)
3. **Static assets**: Deploy frontend separately for better performance

## Troubleshooting

### Common Issues:

1. **Database Connection Errors**:
   - Verify PostgreSQL service is running
   - Check DATABASE_URL environment variable
   - Ensure Railway database plugin is added

2. **JWT Authentication Issues**:
   - Verify JWT_SECRET is set and secure
   - Check token expiration settings
   - Clear browser storage and try again

3. **Build Failures**:
   - Check Railway build logs
   - Verify Dockerfile syntax
   - Ensure all dependencies are in pom.xml

4. **Health Check Failures**:
   - Verify `/actuator/health` endpoint responds
   - Check application startup logs
   - Ensure proper Spring Boot Actuator configuration

### Debug Commands:
```bash
# Railway CLI commands
railway login
railway link [project-id]
railway logs
railway shell
railway status
```

## Deployment Checklist

### Pre-Deployment:
- [ ] Code pushed to GitHub
- [ ] JWT_SECRET generated and secure
- [ ] Database requirements documented
- [ ] Frontend API URL configured

### Railway Setup:
- [ ] Railway project created
- [ ] GitHub repository connected
- [ ] PostgreSQL service added
- [ ] Environment variables configured
- [ ] Custom domain configured (optional)

### Post-Deployment:
- [ ] Application health check passes
- [ ] Default admin login works
- [ ] Admin password changed
- [ ] Frontend can communicate with backend
- [ ] All features tested in production

## Production Best Practices

1. **Security**:
   - Use strong JWT secrets (256+ bits)
   - Enable HTTPS (Railway default)
   - Regularly update dependencies

2. **Monitoring**:
   - Set up Railway notifications
   - Monitor application logs
   - Check database performance

3. **Backup Strategy**:
   - Railway provides automatic database backups
   - Export important data regularly
   - Document recovery procedures

4. **Scaling**:
   - Monitor resource usage
   - Scale database as user base grows
   - Consider horizontal scaling for high traffic

## Support and Resources

- **Railway Documentation**: https://docs.railway.app/
- **Railway Discord**: Community support
- **Application Logs**: Available in Railway dashboard
- **Health Monitoring**: `/actuator/health` endpoint

Your Shift Scheduler application is now production-ready for Railway deployment!
