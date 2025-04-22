## not working
FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn clean package

FROM eclipse-temurin:17
COPY /usr/src/app/target/akka-finance-project-1.0-SNAPSHOT.jar /usr/app/akka-finance-project-1.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/akka-finance-project-1.0-SNAPSHOT.jar"]