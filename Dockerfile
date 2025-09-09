# syntax=docker/dockerfile:1.7
ARG MAVEN_IMAGE=maven:3-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre

FROM ${MAVEN_IMAGE} AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests=true clean package

FROM ${RUNTIME_IMAGE}
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
