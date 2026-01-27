#!/bin/bash
set -e

echo "Railway start script starting..."

# Check if JAR file exists
if [ ! -f "target/shift-scheduler-backend-1.0.0.jar" ]; then
    echo "ERROR: JAR file not found!"
    echo "Looking for any JAR files:"
    find . -name "*.jar" -type f || echo "No JAR files found"
    exit 1
fi

echo "Starting Spring Boot application..."
exec java \
    -server \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=railway \
    -jar target/shift-scheduler-backend-1.0.0.jar
