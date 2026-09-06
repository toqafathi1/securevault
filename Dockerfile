# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: runtime ----
# JRE only, not a full JDK -- the build toolchain never ships in the final image.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/securevault-0.1.0.jar ./securevault.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "securevault.jar"]
