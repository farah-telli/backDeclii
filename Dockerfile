# ==========================
#   BUILD STAGE
# ==========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

ENV SPRING_PROFILES_ACTIVE=build
WORKDIR /BACKEND

# Copier tout le contexte du projet (plus sûr que deux COPY séparés)
COPY . .

RUN mvn clean package -Dspring.profiles.active=build -DskipTests \
    -DrepoUrl=http://192.168.50.4:8081/repository/maven-snapshots/ \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true

# ==========================
#   RUNTIME STAGE
# ==========================
FROM eclipse-temurin:17-jre-jammy

COPY --from=build /BACKEND/target/BackDeclitech-0.0.1-SNAPSHOT.jar /usr/local/lib/BackDeclitech.jar

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/usr/local/lib/BackDeclitech.jar"]
