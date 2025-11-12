# ==========================
#   BUILD STAGE
# ==========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Désactiver les connexions DB pendant la build
ENV SPRING_PROFILES_ACTIVE=build

# Définir le répertoire de travail
WORKDIR /BACKEND

# Copier les fichiers nécessaires
COPY pom.xml .                # Copie pom.xml dans /BACKEND
COPY src ./src                # Copie le dossier src dans /BACKEND/src

# Compiler et empaqueter le projet
RUN mvn clean package -Dspring.profiles.active=build -DskipTests \
    -DrepoUrl=http://192.168.50.4:8081/repository/maven-snapshots/ \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true

# ==========================
#   RUNTIME STAGE
# ==========================
FROM eclipse-temurin:17-jre-jammy

# Copier le JAR compilé depuis la phase de build
COPY --from=build /BACKEND/target/BackDeclitech-0.0.1-SNAPSHOT.jar /usr/local/lib/BackDeclitech.jar

# Exposer le port de l'application
EXPOSE 8089

# Lancer l'application
ENTRYPOINT ["java", "-jar", "/usr/local/lib/BackDeclitech.jar"]
