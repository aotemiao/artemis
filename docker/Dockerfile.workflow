ARG JAVA_VERSION=21
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

ARG JAR_PATH=artemis-modules/artemis-workflow/artemis-workflow-start/target/*.jar
COPY ${JAR_PATH} app.jar

EXPOSE 9410
ENTRYPOINT ["java", "-jar", "/app.jar"]
