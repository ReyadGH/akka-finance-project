# # FROM
# # WORKDIR /app
# #
# # # copy the built jar from Maven target directory
# # COPY target/*.jar app.jar
# #
# # # run your main class
# # ENTRYPOINT ["java", "-cp", "App.jar", "org.example.App"]
#
#
# # Use an official Maven image as the base image
# FROM maven:3.8.4-openjdk-11-slim AS build
# # Set the working directory in the container
# WORKDIR /app
# # Copy the pom.xml and the project files to the container
# COPY pom.xml .
# COPY src ./src
# # Build the application using Maven
# RUN mvn clean package -DskipTests
# # Use an official OpenJDK image as the base image
# FROM eclipse-temurin:17
# # Set the working directory in the container
# WORKDIR /app
# # Copy the built JAR file from the previous stage to the container
# COPY -from=build /app/target/my-application.jar .
# # Set the command to run the application
# CMD ["java", "-jar", "my-application.jar"]
#

FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn clean package

FROM eclipse-temurin:17
COPY --from=build /usr/src/app/target/demo-0.0.1-SNAPSHOT.jar /usr/app/demo-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/demo-0.0.1-SNAPSHOT.jar"]