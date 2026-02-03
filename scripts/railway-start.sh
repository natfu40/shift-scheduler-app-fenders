#!/bin/bash
set -e

echo "Railway start script starting..."
echo "Available memory: $(free -h | grep '^Mem:' | awk '{print $7}' 2>/dev/null || echo 'Unknown')"

# Check if JAR file exists
if [ ! -f "target/shift-scheduler-backend-1.0.0.jar" ]; then
    echo "ERROR: JAR file not found!"
    echo "Looking for any JAR files:"
    find . -name "*.jar" -type f || echo "No JAR files found"
    exit 1
fi

# Set memory settings based on available memory
TOTAL_MEMORY=$(free -m 2>/dev/null | awk '/^Mem:/{print $2}' || echo "1024")
echo "Total system memory: ${TOTAL_MEMORY}MB"

if [ "$TOTAL_MEMORY" -lt 1024 ]; then
    MAX_HEAP=60
    INITIAL_HEAP=30
    echo "Low memory environment - using conservative JVM settings"
elif [ "$TOTAL_MEMORY" -lt 2048 ]; then
    MAX_HEAP=65
    INITIAL_HEAP=40
    echo "Medium memory environment - using moderate JVM settings"
else
    MAX_HEAP=70
    INITIAL_HEAP=50
    echo "Adequate memory environment - using standard JVM settings"
fi

echo "Starting Spring Boot application with MaxRAM=${MAX_HEAP}%, InitialRAM=${INITIAL_HEAP}%..."
exec java \
    -server \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=${MAX_HEAP}.0 \
    -XX:InitialRAMPercentage=${INITIAL_HEAP}.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+ParallelRefProcEnabled \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=railway \
    -jar target/shift-scheduler-backend-1.0.0.jar
