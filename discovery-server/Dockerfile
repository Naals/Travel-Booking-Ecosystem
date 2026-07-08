# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy parent POM and shared module POMs first — Docker layer cache
COPY pom.xml                        ./pom.xml
COPY shared-kernel/pom.xml          ./shared-kernel/pom.xml
COPY common-lib/pom.xml             ./common-lib/pom.xml
COPY discovery-server/pom.xml       ./discovery-server/pom.xml

# Resolve dependencies before copying source (cache-friendly)
RUN mvn dependency:go-offline \
    -pl shared-kernel,common-lib,discovery-server -am -q 2>/dev/null || true

# Copy sources
COPY shared-kernel/src   ./shared-kernel/src
COPY common-lib/src      ./common-lib/src
COPY discovery-server/src ./discovery-server/src

# Build — skip tests (run separately in CI)
RUN mvn package -pl shared-kernel,common-lib,discovery-server -am -DskipTests -q

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/discovery-server/target/*.jar app.jar

EXPOSE 8761

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
