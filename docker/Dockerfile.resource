ARG JAVA_VERSION=21
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

ARG JAR_PATH=artemis-modules/artemis-resource/artemis-resource-start/target/*.jar
COPY ${JAR_PATH} app.jar

EXPOSE 9400
ENTRYPOINT ["java", "-jar", "/app.jar"]
