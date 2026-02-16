FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY core-typing/pom.xml core-typing/
COPY avatar-module/pom.xml avatar-module/
COPY app/pom.xml app/

RUN mvn dependency:go-offline

COPY core-typing/src core-typing/src
COPY avatar-module/src avatar-module/src
COPY app/src app/src

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system springgroup && adduser --system springuser --ingroup springgroup

COPY --from=builder --chown=springuser:springgroup --chmod=0550 /app/app/target/app-0.0.1-SNAPSHOT.jar ./app.jar

USER springuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]