# ---- Build stage: compile and package with Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Build
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Runtime stage: slim JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Keep heap small enough for free 512MB instances
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
