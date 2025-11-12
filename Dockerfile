# ==========================
#   BUILD STAGE
# ==========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

ENV SPRING_PROFILES_ACTIVE=build

# Définir le répertoire de travail
WORKDIR /app

# Copier uniquement le pom.xml d'abord pour tirer parti du cache Docker
COPY pom.xml .

# Télécharger les dépendances Maven (sera mis en cache)
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Compiler le projet (tests désactivés)
RUN mvn clean package -Dspring.profiles.active=build -DskipTests

# ==========================
#   RUNTIME STAGE
# ==========================
FROM eclipse-temurin:17-jre-jammy

# Répertoire d'exécution
WORKDIR /app

# Copier le jar depuis la phase de build
COPY --from=build /app/target/BackDeclitech-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port
EXPOSE 8089

# Lancer l’application
ENTRYPOINT ["java", "-jar", "app.jar"]
