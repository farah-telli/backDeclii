# Build Stage
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Set active profile to 'build' to disable DB connection during tests
ENV SPRING_PROFILES_ACTIVE=build

# Copy source code and pom.xml into the container
COPY src 
COPY pom.xml

# Build the project, skipping tests
RUN mvn -f /BACKEND/pom.xml clean package -Dspring.profiles.active=build -DskipTests \
    -DrepoUrl=http://192.168.50.4:8081//repository/maven-snapshots/ \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true

# Package Stage
FROM openjdk:17-slim

# Copy the built jar file from the build stage
COPY --from=build /target/BackDeclitech-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar

# Expose app's port
EXPOSE 8089

# Run the app
ENTRYPOINT ["java", "-jar", "/usr/local/lib/demo.jar"]
