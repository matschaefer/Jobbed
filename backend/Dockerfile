# ============================================================================
# Backend – Multi-Stage-Build
# Stage 1: Build mit Maven (Tests laufen separat in CI, hier -DskipTests)
# Stage 2: schlankes JRE-Runtime-Image, non-root
# ============================================================================

# ----- Stage 1: Build -----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Abhängigkeiten zuerst cachen
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Quellcode kompilieren und paketieren
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ----- Stage 2: Runtime -----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# wget für Healthcheck ist in Alpine (busybox) enthalten.
RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/uploads && chown -R app:app /app
USER app

COPY --from=build /build/target/jobbed-backend.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
