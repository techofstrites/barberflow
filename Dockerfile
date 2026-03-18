# ---- Build Stage ----
FROM gradle:8.11-jdk21-alpine AS builder
WORKDIR /app

# Copy dependency files first for layer caching
COPY build.gradle.kts settings.gradle.kts ./
COPY core/build.gradle.kts core/build.gradle.kts
COPY modules/iam/build.gradle.kts modules/iam/build.gradle.kts
COPY modules/scheduling/build.gradle.kts modules/scheduling/build.gradle.kts
COPY modules/customer/build.gradle.kts modules/customer/build.gradle.kts
COPY modules/chatbot/build.gradle.kts modules/chatbot/build.gradle.kts
COPY modules/recommendation/build.gradle.kts modules/recommendation/build.gradle.kts
COPY modules/billing/build.gradle.kts modules/billing/build.gradle.kts
COPY app/build.gradle.kts app/build.gradle.kts

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon --quiet || true

# Copy source code
COPY . .

# Build the fat JAR
RUN gradle :app:bootJar --no-daemon -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
