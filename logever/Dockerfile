FROM maven:3.6.0-jdk-11-slim AS build
COPY logever/pom.xml /app/
COPY logever/src /app/src
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml clean package -DskipTests

FROM adoptopenjdk/openjdk11:latest
COPY --from=build /app/target/logever-0.0.1-SNAPSHOT.jar /app/confacai.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=container","/app/confacai.jar"]