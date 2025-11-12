# ==========================
#   BUILD STAGE
# ==========================
FROM maven:3.8.4-openjdk-17-slim AS build

# Profil Spring actif
ENV SPRING_PROFILES_ACTIVE=build

# Répertoire de travail
WORKDIR /app

# Copier uniquement le pom.xml pour exploiter le cache Docker
COPY pom.xml .

# Pré-télécharger toutes les dépendances Maven
RUN mvn -B dependency:go-offline \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true

# Copier le code source après pour ne pas casser le cache
COPY src ./src

# Compiler et packager le projet (tests désactivés)
RUN mvn -B clean package -Dspring.profiles.active=build -DskipTests \
    -DrepoUrl=http://192.168.50.4:8081/repository/maven-snapshots/ \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true

# ==========================
#   RUNTIME STAGE
# ==========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier uniquement le jar final depuis la phase de build
COPY --from=build /app/target/BackDeclitech-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port
EXPOSE 8089

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
