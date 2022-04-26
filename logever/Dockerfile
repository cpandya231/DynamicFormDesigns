FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp
EXPOSE 8080
ADD ./target/logever-0.0.1-SNAPSHOT.jar confacai.jar
ENTRYPOINT ["java","-jar","confacai.jar"]