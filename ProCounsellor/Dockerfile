# Use Eclipse Temurin JDK for Java 17 (or your required version)
FROM eclipse-temurin:17-jdk as build

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Use a lightweight JDK for production
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
