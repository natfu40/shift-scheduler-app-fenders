# Multi-stage build for production optimization
FROM maven:3.9-amazoncorretto-17 AS builder

WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests -B

# Production runtime image
FROM amazoncorretto:17-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -G appuser -u 1001

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/target/shift-scheduler-backend-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Railway-compatible JVM settings (simplified)
ENTRYPOINT ["java", \
    "-server", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=70.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=100", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:railway}", \
    "-jar", "app.jar"]
