# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent POM
COPY pom.xml .

# Copy modules
COPY autho-forge-service ./autho-forge-service
COPY autho-forge-starter ./autho-forge-starter

# Build the service module (and dependencies)
RUN mvn clean package -pl autho-forge-service -am -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/autho-forge-service/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Environment variables
ENV AUTHO_FORGE_KEY_CONTENT=""
ENV AUTHO_FORGE_KEY_PASSPHRASE=""

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
