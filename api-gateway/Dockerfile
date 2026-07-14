# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml                  ./pom.xml
COPY shared-kernel/pom.xml    ./shared-kernel/pom.xml
COPY common-lib/pom.xml       ./common-lib/pom.xml
COPY api-gateway/pom.xml      ./api-gateway/pom.xml

RUN mvn dependency:go-offline \
    -pl shared-kernel,common-lib,api-gateway -am -q 2>/dev/null || true

COPY shared-kernel/src  ./shared-kernel/src
COPY common-lib/src     ./common-lib/src
COPY api-gateway/src    ./api-gateway/src

RUN mvn package -pl shared-kernel,common-lib,api-gateway -am -DskipTests -q

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/api-gateway/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
