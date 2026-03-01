#!/bin/bash
# Railway build script for Shift Scheduler Backend

set -e

echo "Building Shift Scheduler Backend..."

# Clean and build the application using Maven
echo "Running Maven build..."
mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17

echo "Build completed successfully!"
echo "JAR file location: target/shift-scheduler-app-fenders-1.0.0.jar"
