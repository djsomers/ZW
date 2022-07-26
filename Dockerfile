# syntax=docker/dockerfile:1

FROM openjdk:17-oracle

WORKDIR /assessment

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw"]
