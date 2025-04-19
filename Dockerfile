FROM eclipse-temurin:17

WORKDIR /app

# copy the built jar from Maven target directory
COPY target/*.jar app.jar

# run your main class
ENTRYPOINT ["java", "-cp", "app.jar", "org.example.App"]
