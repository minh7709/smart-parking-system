# Stage 1: Build bằng Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy pom.xml trước để cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Chạy Jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Healthcheck cho Spring Boot
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]