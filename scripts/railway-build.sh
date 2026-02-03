#!/bin/bash
set -e

echo "Railway build script starting..."
echo "Available memory: $(free -h | grep '^Mem:' | awk '{print $2}' 2>/dev/null || echo 'Unknown')"
echo "Java version:"
java -version
echo "Maven version:"
mvn -version

# Set Maven memory options for build
export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=256m"
echo "Maven memory settings: $MAVEN_OPTS"

echo "Building project with Maven..."
mvn clean package -DskipTests -B

echo "Checking for built JAR file..."
ls -la target/

echo "Build completed successfully!"
