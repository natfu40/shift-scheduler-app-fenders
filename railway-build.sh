#!/bin/bash
set -e

echo "Railway build script starting..."
echo "Java version:"
java -version
echo "Maven version:"
mvn -version

echo "Building project with Maven..."
mvn clean package -DskipTests -B

echo "Checking for built JAR file..."
ls -la target/

echo "Build completed successfully!"
