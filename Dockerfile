# ==========================
#   BUILD STAGE (léger)
# ==========================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

ENV SPRING_PROFILES_ACTIVE=build
WORKDIR /app

# Copier le pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copier le code source
COPY src ./src

# Compiler le projet (tests désactivés)
RUN mvn -B clean package -Dspring.profiles.active=build -DskipTests

# ==========================
#   RUNTIME STAGE (très léger)
# ==========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/BackDeclitech-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
