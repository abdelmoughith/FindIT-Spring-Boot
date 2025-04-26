# ========== Build Stage ==========
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom and download dependencies first for caching
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN ./mvnw dependency:go-offline

# Copy source and build the app
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ========== Runtime Stage ==========
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/findit-spring-boot-0.0.1-SNAPSHOT.jar app.jar

# Copy Firebase token file
COPY src/main/resources/indus-532b7-firebase-adminsdk-uhb0r-e05db78a77.json /app/

# Expose the port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
