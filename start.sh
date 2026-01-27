#!/bin/bash
# Railway start script for Shift Scheduler Backend

set -e

echo "Starting Shift Scheduler Backend..."

# Build the application if jar doesn't exist
if [ ! -f "backend/target/shift-scheduler-backend-1.0.0.jar" ]; then
    echo "Building application..."
    mvn clean package -DskipTests -pl backend -am -Dmaven.compiler.source=17 -Dmaven.compiler.target=17
fi

# Start the application
echo "Starting Spring Boot application..."
exec java \
    -server \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=railway \
    -jar backend/target/shift-scheduler-backend-1.0.0.jar
