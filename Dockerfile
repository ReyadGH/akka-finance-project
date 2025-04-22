## not working
FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn clean package

FROM eclipse-temurin:17
COPY --from=build /usr/src/app/target/demo-0.0.1-SNAPSHOT.jar /usr/app/demo-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/demo-0.0.1-SNAPSHOT.jar"]