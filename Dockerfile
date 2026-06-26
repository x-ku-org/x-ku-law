# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Cache dependency layer separately to speed up rebuilds
COPY pom.xml .
COPY lr-common/pom.xml lr-common/
COPY lr-module-system/pom.xml lr-module-system/
COPY lr-module-collect/pom.xml lr-module-collect/
COPY lr-module-law/pom.xml lr-module-law/
COPY lr-module-search/pom.xml lr-module-search/
COPY lr-module-ai/pom.xml lr-module-ai/
COPY lr-module-subscription/pom.xml lr-module-subscription/
COPY lr-module-compliance/pom.xml lr-module-compliance/
COPY lr-module-workspace/pom.xml lr-module-workspace/
COPY lr-module-content/pom.xml lr-module-content/
COPY lr-module-support/pom.xml lr-module-support/
COPY lr-module-openapi/pom.xml lr-module-openapi/
COPY lr-server/pom.xml lr-server/
RUN mvn dependency:go-offline -q

# Copy sources and build
COPY . .
RUN mvn clean package -DskipTests -pl lr-server -am -q

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

COPY --from=builder /build/lr-server/target/lr-server-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
