FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . .

RUN mvn -B -pl artemis-modules/artemis-workflow/artemis-workflow-start -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/artemis-modules/artemis-workflow/artemis-workflow-start/target/artemis-workflow-start-*.jar /app/app.jar

EXPOSE 9410
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
