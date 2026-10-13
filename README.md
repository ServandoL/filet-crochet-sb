# Filet Crochet — Spring Boot API

Small Spring Boot project for working with crochet patterns and progress
tracking. It is the Java implementation of the API behind
[filet-crochet-grid](https://github.com/ServandoL/filet-crochet-grid).

## Tech stack
- Java 25, Spring Boot 4.1, Gradle (wrapper included)
- Spring Data MongoDB, Bean Validation, Actuator
- Docker (Eclipse Temurin 25 minimal image)

## What it provides
- Pattern CRUD endpoints (`PatternsController`) with DTOs for create/update
- Progress tracking (upsert progress per pattern)
- Custom repositories on top of Spring Data MongoDB
- Validation, including a custom `@ValidObjectId` validator for MongoDB ids
- Global exception handling, CORS config, and request logging

## Quick start
Prerequisites: Java 17+ (project toolchain is Java 25) — the Gradle wrapper is
included, no local Gradle install needed.

```bash
./gradlew test       # run tests
./gradlew bootRun    # run the application
```

## Configuration
The app reads the MongoDB connection URI from the `MONGO_URI` property;
`src/main/resources/application.properties` provides a local fallback default.
In Docker, `MONGO_URI`, `MONGO_DATABASE`, `CORS_ORIGINS`, and `SERVER_PORT`
(default 9002) are supplied as environment variables.
