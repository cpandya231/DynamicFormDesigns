FROM maven:3.6.0-jdk-11-slim AS build
COPY pom.xml /app/
COPY src /app/src
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp
EXPOSE 8080
COPY --from=build /app/target/logever-0.0.1-SNAPSHOT.jar /app/confacai.jar
ENTRYPOINT ["java","-jar","/app/confacai.jar"]