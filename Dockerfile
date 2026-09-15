# syntax=docker/dockerfile:1

# ---------- build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Wrapper + POM first so the dependency layer is cached independently of source changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Build the executable jar. Tests run in CI (GitHub Actions), so skip them here for fast images.
# BuildKit cache mount keeps the local Maven repo warm across builds.
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B clean package -DskipTests

# ---------- run stage ----------
FROM eclipse-temurin:21-jre AS run
WORKDIR /app

# Run as a non-root user.
RUN useradd -r -u 1001 spring
COPY --from=build /app/target/*.jar app.jar
USER spring

# Railway injects $PORT at runtime; application.properties binds server.port to it.
EXPOSE 9091
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
