FROM openjdk:21-jdk-slim AS builder

FROM openjdk:21-jdk-slim AS production
COPY /target/*.jar /depth-chart-manager-0.0.1.jar
ENTRYPOINT ["java","-jar","/depth-chart-manager-0.0.1.jar"]