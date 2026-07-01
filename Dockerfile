# syntax=docker/dockerfile:1

##############################################################################
# Stage 1 — build the Spring Boot fat jar
##############################################################################
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy the Maven wrapper and POM first. This layer is only invalidated when
# the build definition changes, so dependency downloads stay cached across
# source-only changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

# Now copy the sources and build. Tests run in the CI pipeline (see
# .github/workflows/docker-publish.yml), so they are skipped here to keep the
# image build fast and free of the Playwright browser download.
COPY src/ src/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests clean package

# Explode the jar into Spring Boot layers so the runtime image can cache the
# rarely-changing dependency layers separately from the application code. The
# "application" layer holds a runnable jar whose manifest Class-Path points at
# the externalized "lib/" (the dependencies layer); rename it to a stable name
# so the runtime ENTRYPOINT doesn't hard-code the version.
RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination extracted \
    && mv extracted/application/*.jar extracted/application/app.jar

##############################################################################
# Stage 2 — minimal runtime image
##############################################################################
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Run as an unprivileged user rather than root.
RUN groupadd --system spring && useradd --system --gid spring spring

# Copy the exploded layers ordered from least to most frequently changed so
# Docker can reuse layers between builds. The dependencies layer provides
# "lib/"; the application layer provides "app.jar".
COPY --from=build --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/application/ ./

USER spring:spring

EXPOSE 8080

# The app has no Actuator endpoint, so the check just confirms the HTTP port
# is accepting connections (uses bash's /dev/tcp, no extra packages needed).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD bash -c ': > /dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
