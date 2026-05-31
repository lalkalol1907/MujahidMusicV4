FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon clean shadowJar

FROM eclipse-temurin:24-jre AS runtime
WORKDIR /app

RUN groupadd --system mujahid && useradd --system --gid mujahid --home /app mujahid

COPY --from=build /app/build/libs/MujahidMusicV4.jar app.jar
USER mujahid

ENV JAVA_OPTS="" \
    HEALTH_FILE="/tmp/mujahid-health"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD test -f "$HEALTH_FILE" && [ $(( $(date +%s) - $(stat -c %Y "$HEALTH_FILE") )) -lt 60 ] || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
