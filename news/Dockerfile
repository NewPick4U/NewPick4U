# Dockerfile
FROM amazoncorretto:17-alpine
WORKDIR /app
ENV APP_NAME=news-0.0.1-SNAPSHOT.jar
COPY build/libs/${APP_NAME} app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
