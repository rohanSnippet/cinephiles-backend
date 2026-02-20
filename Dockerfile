FROM eclipse-temurin:latest

WORKDIR /app

# Copy built JAR from build stage
COPY target/*.jar app.jar

EXPOSE 9000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
