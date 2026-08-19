FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml                    ./pom.xml
COPY shared-kernel/pom.xml      ./shared-kernel/pom.xml
COPY common-lib/pom.xml         ./common-lib/pom.xml
COPY messaging-service/pom.xml  ./messaging-service/pom.xml

RUN mvn dependency:go-offline \
    -pl shared-kernel,common-lib,messaging-service -am -q 2>/dev/null || true

COPY shared-kernel/src     ./shared-kernel/src
COPY common-lib/src        ./common-lib/src
COPY messaging-service/src ./messaging-service/src

RUN mvn package -pl shared-kernel,common-lib,messaging-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/messaging-service/target/*.jar app.jar

EXPOSE 8092

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8092/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
