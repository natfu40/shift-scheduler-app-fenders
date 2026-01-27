#!/bin/bash
# Railway start script for Shift Scheduler Backend

set -e

echo "Starting Shift Scheduler Backend..."

# Build the application if jar doesn't exist
if [ ! -f "target/shift-scheduler-backend-1.0.0.jar" ]; then
    echo "Building application..."
    mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17
fi

# Check what JAR files are available
echo "Checking for JAR files in target:"
ls -la target/ || echo "target directory not found"

# Find the correct JAR file
JAR_FILE=$(find target -name "*.jar" -not -name "*sources.jar" -not -name "*javadoc.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in target/"
    echo "Available files:"
    find . -name "*.jar" -type f 2>/dev/null || echo "No JAR files found anywhere"
    exit 1
fi

echo "Found JAR file: $JAR_FILE"

# Start the application
echo "Starting Spring Boot application..."
exec java \
    -server \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=railway \
    -jar "$JAR_FILE"
