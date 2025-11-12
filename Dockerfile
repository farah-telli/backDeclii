# ==========================
#   BUILD STAGE (Maven + JDK 17)
# ==========================
FROM maven:3.8.4-openjdk-17-slim AS build

ENV SPRING_PROFILES_ACTIVE=build
WORKDIR /app

# Copier uniquement le pom.xml pour utiliser le cache Docker
COPY pom.xml .

# Télécharger les dépendances Maven (pré-cache)
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
#   RUNTIME STAGE (ultra-léger)
# ==========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier uniquement le jar final
COPY --from=build /app/target/BackDeclitech-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
