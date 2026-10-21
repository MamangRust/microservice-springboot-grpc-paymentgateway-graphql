# syntax=docker/dockerfile:1.7

# ---------- Stage 1: build ----------
# Build context is the payment_gateway/ multi-module root:
#   docker build -f stats-writer/Dockerfile -t springboot-paymentgateway/stats-writer:latest .
# NOTE: the build stage must be glibc-based (Debian/Ubuntu), NOT Alpine.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
COPY common/pom.xml common/
COPY role/pom.xml role/
COPY user/pom.xml user/
COPY merchant/pom.xml merchant/
COPY card/pom.xml card/
COPY saldo/pom.xml saldo/
COPY topup/pom.xml topup/
COPY transaction/pom.xml transaction/
COPY transfer/pom.xml transfer/
COPY withdraw/pom.xml withdraw/
COPY auth/pom.xml auth/
COPY gateway/pom.xml gateway/
COPY email-service/pom.xml email-service/
COPY db-migration/pom.xml db-migration/
COPY stats-reader/pom.xml stats-reader/
COPY stats-writer/pom.xml stats-writer/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl stats-writer -am dependency:go-offline

COPY common/src common/src
COPY stats-writer/src stats-writer/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl stats-writer -am -DskipTests package

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -g 1001 -S app \
    && adduser -u 1001 -S app -G app \
    && mkdir -p /var/log/app \
    && chown -R 1001:1001 /app /var/log/app

COPY --from=build --chown=1001:1001 \
    /workspace/stats-writer/target/stats-writer-0.0.1-SNAPSHOT.jar /app/app.jar

USER 1001:1001

# No server ports: Kafka consumer (or one-shot backfill job).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
