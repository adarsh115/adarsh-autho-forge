# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Skip tests to speed up build, assuming tests ran in CI
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Environment variables
ENV AUTHO_FORGE_KEY_CONTENT=""
ENV AUTHO_FORGE_KEY_PASSPHRASE=""

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
