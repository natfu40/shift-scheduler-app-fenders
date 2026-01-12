FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /app

# Copy build files
COPY pom.xml .
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Use a smaller image for runtime
FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/target/shift-scheduler-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

