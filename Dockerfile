# ---------- Build stage ----------
# Compiles the app inside a container with Maven + JDK 21 already installed,
# so the final image never needs a build toolchain baked into it.
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy only the POM first so Docker can cache the dependency-download layer
# separately from source code changes (classic Docker layer-caching trick).
COPY pom.xml .
RUN mvn -B dependency:go-offline || true

COPY src src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
# Slim JRE-only image — no Maven, no JDK, no source code, much smaller footprint.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as a non-root user — standard container security practice.
RUN addgroup -S taskflow && adduser -S taskflow -G taskflow
USER taskflow

COPY --from=build /app/target/taskflow-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]