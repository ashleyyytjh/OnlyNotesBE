# Build .jar file
FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests



FROM eclipse-temurin:17-jdk-alpine
# Use non-root system user for security
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir /uploads && chown -R spring:spring /uploads
USER spring:spring

# Copy OpenTelemetry Java Agent
COPY opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

# ARG JAR_FILE=target/*.jar
COPY --from=build /target/notes-0.0.1-SNAPSHOT.jar .

EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "notes-0.0.1-SNAPSHOT.jar"]