FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml                   ./pom.xml
COPY shared-kernel/pom.xml     ./shared-kernel/pom.xml
COPY common-lib/pom.xml        ./common-lib/pom.xml
COPY identity-service/pom.xml  ./identity-service/pom.xml

RUN mvn dependency:go-offline \
    -pl shared-kernel,common-lib,identity-service -am -q 2>/dev/null || true

COPY shared-kernel/src   ./shared-kernel/src
COPY common-lib/src      ./common-lib/src
COPY identity-service/src ./identity-service/src

RUN mvn package -pl shared-kernel,common-lib,identity-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/identity-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
