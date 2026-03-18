# Multi-stage Dockerfile for Spring Boot (Maven, Java 17)

# ===== Build Stage =====
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Optimize dependency layer caching
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -DskipTests package

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre


WORKDIR /app

# Copy built artifact
# Copies the only jar produced by the build to /app/app.jar
COPY --from=builder /workspace/target/*.jar /app/app.jar

# Expose application port
EXPOSE 8090

# Optional JVM args can be injected via JAVA_OPTS
ENV JAVA_OPTS=""

# Run the application
# SERVER_PORT is forwarded to Spring via -Dserver.port
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-${SERVER_PORT:-8090}} -jar /app/app.jar"]
