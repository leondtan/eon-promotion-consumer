#
# Package stage
#
FROM openjdk:11-jre-slim

ARG JAR_FILE=target/promotion-consumer-1.0-jar-with-dependencies.jar

COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java","-jar","application.jar"]
