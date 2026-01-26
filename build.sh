#!/bin/bash
# Railway build script for Shift Scheduler Backend

set -e

echo "Building Shift Scheduler Backend..."

# Clean and build the application using Maven modules
echo "Running Maven build..."
mvn clean package -DskipTests -pl backend -am -Dmaven.compiler.source=21 -Dmaven.compiler.target=21

echo "Build completed successfully!"
echo "JAR file location: backend/target/shift-scheduler-backend-1.0.0.jar"
