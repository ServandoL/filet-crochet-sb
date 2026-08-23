FROM eclipse-temurin:25-jdk-ubi10-minimal
LABEL authors="servandoluviano"

# Allow specifying which built jar to copy in at build time. By default we use the
# assembled Spring Boot jar created by the Gradle build (see build/libs/).
ARG JAR_FILE=build/libs/filet-crochet-0.0.1-SNAPSHOT.jar
WORKDIR /app

# Copy the application jar (produced by `./gradlew bootJar` or the build pipeline)
COPY ${JAR_FILE} ./app.jar

# Provide sane defaults that mirror src/main/resources/application.properties so
# the container can run without extra env vars. These can be overridden at runtime
# with `docker run -e MONGO_URI=...` etc.
ENV MONGO_APP_URL="mongodb://localhost:27017/test" \
	MONGO_DATABASE="test" \
	CORS_ORIGINS="http://localhost:4200,http://localhost:5173" \
	SERVER_PORT=9002 \
	JAVA_OPTS=""

EXPOSE 9002

# Start the Spring Boot application. Users can pass additional JVM flags via
# the JAVA_OPTS env var and override env-specific values with -e when running.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
