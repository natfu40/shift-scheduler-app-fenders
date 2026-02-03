# Memory Optimization Fixes for Railway Deployment

This document outlines all the changes made to resolve out-of-memory errors in both CI/CD pipeline and Railway backend deployment.

## Issues Identified
1. **Maven Surefire plugin** running with insufficient memory
2. **TestContainers** consuming excessive memory during CI
3. **Spring Boot application** using too much heap memory on Railway
4. **Parallel test execution** competing for limited memory
5. **Database connection pools** sized too large for memory constraints

## Changes Made

### 1. Maven Configuration (pom.xml)
- **Updated Surefire Plugin** with memory optimization:
  - Set `-Xmx2048m` for test execution
  - Replaced deprecated `-XX:MaxPermSize` with `-XX:MaxMetaspaceSize=512m` for Java 17
  - Added G1GC for better memory management
  - Disabled parallel test execution with `forkCount=1` and `reuseForks=true`
  - Added TestContainers optimization properties

### 2. Test Configuration (application-test.properties)
- **Optimized Database Settings**:
  - Reduced Hikari connection pool to `maximum-pool-size=2`
  - Set shorter timeouts to free resources faster
  - Configured H2 in PostgreSQL compatibility mode
- **Memory-Conscious Logging**:
  - Reduced log levels to WARN for most components
  - Disabled unnecessary Spring features during tests

### 3. Railway Deployment Configuration

#### Nixpacks Configuration (nixpacks.toml)
- **JVM Memory Settings**:
  - Reduced `MaxRAMPercentage` from 75% to 70%
  - Added `InitialRAMPercentage=50%` to prevent early memory spikes
  - Added advanced GC tuning parameters

#### Dockerfile Updates (deployment/Dockerfile)
- **Production JVM Optimization**:
  - Server-class JVM with container support
  - G1GC with 100ms pause time target
  - String deduplication and parallel reference processing
  - Proper memory percentage settings

#### Railway Application Properties (application-railway.properties)
- **Connection Pool Optimization**:
  - Reduced maximum pool size from 5 to 3
  - Added leak detection threshold
  - Shorter connection timeouts
- **Tomcat Thread Pool Limits**:
  - Max threads: 20 (down from default 200)
  - Min spare threads: 5
  - Accept count: 100

#### Railway Configuration (railway.json)
- **Resource Limits**:
  - Set explicit memory limit: 1Gi
  - Set CPU limit: 0.5 cores
  - Increased health check timeout to 120s

### 4. Scripts Optimization

#### Railway Start Script (scripts/railway-start.sh)
- **Adaptive Memory Management**:
  - Detects available system memory
  - Adjusts JVM heap size based on available resources:
    - < 1GB: 60% max heap, 30% initial
    - 1-2GB: 65% max heap, 40% initial  
    - > 2GB: 70% max heap, 50% initial
- **Memory Monitoring**:
  - Displays available memory on startup
  - Shows calculated JVM settings

#### Build Script (scripts/railway-build.sh)
- **Build-Time Memory Optimization**:
  - Set `MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=256m"`
  - Display available memory during build

### 5. CI/CD Pipeline (.github/workflows/ci-cd.yml)
- **Simplified Test Execution**:
  - Removed complex fallback logic
  - Set proper `MAVEN_OPTS` with Java 17 compatible parameters
  - Added TestContainers optimization environment variables
  - Streamlined test result verification

## Memory Allocation Strategy

### Development/Local Testing
- **Test JVM**: 2GB max heap, 512MB metaspace
- **H2 Database**: In-memory with minimal connection pool
- **Single-threaded test execution** to avoid memory competition

### CI/CD Environment (GitHub Actions)
- **Build JVM**: 3GB max heap, 512MB metaspace
- **TestContainers**: Disabled resource checks and cleanup daemon
- **Fallback Strategy**: H2-only tests if full integration tests fail

### Production (Railway)
- **Application JVM**: Adaptive 60-70% of container memory
- **Connection Pool**: 3 max connections with leak detection
- **Health Checks**: Extended timeout for startup under memory pressure
- **Resource Limits**: 1GB memory cap, 0.5 CPU cores

## Expected Improvements
1. **Reduced OOM Errors**: Proper memory limits and GC tuning
2. **Faster Startup**: Optimized connection pools and thread limits  
3. **Better Monitoring**: Built-in memory metrics and health checks
4. **Stable CI/CD**: Reliable test execution with fallback strategies
5. **Cost Efficiency**: Better resource utilization on Railway

## Monitoring Recommendations
- Monitor `/actuator/health` endpoint for memory warnings
- Watch Railway application logs for GC activity
- Set up alerts for memory usage > 85%
- Consider upgrading Railway plan if consistent memory pressure occurs

## Next Steps
1. Test the changes in a Railway deployment
2. Monitor memory usage patterns over time
3. Fine-tune JVM parameters based on actual usage
4. Consider adding custom memory metrics if needed
