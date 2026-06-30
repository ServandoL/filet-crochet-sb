# Filet Crochet

Small Spring Boot project for working with crochet patterns and progress tracking.

## Quick start

Prerequisites:
- Java 17+ (or the project's configured JDK)
- Gradle (the wrapper is included)

Run tests:

```bash
./gradlew test
```

Run the application (uses Spring Boot):

```bash
./gradlew bootRun
```

## Configuration

This project reads the MongoDB connection URI from the `MONGO_URI` property. In `src/main/resources/application.properties` we provide a fallback default so the application context can start when `MONGO_URI` is not set:

```
spring.data.mongodb.uri=${MONGO_URI:mongodb://localhost:27017/test}
```

What this means:
- Spring will use the value of the `MONGO_URI` environment variable (or system property / CLI property) if present.
- If `MONGO_URI` is not set, the default `mongodb://localhost:27017/test` will be used.
- If `MONGO_URI` is set but empty (e.g. `MONGO_URI=""`), Spring will use the empty value — it will not fall back to the default. To avoid this, don't set `MONGO_URI` in your environment or CI unless it contains a valid value.

Examples:
- Use default (no env var set):

```bash
./gradlew test
```

- Override at runtime using an environment variable:

```bash
MONGO_URI='mongodb://user:pass@db.example:27017/production' ./gradlew bootRun
```

- Override with a Java system property:

```bash
./gradlew -DMONGO_URI='mongodb://user:pass@db.example:27017/production' bootRun
```

## Testing notes

- Unit tests mock `MongoTemplate` and do not require a running MongoDB instance.
- The project contains a sensible default Mongo URI to prevent Spring from failing to resolve the property when running tests locally.

## Security

- Do not commit real database credentials into the repository. Use CI environment variables or a secrets manager for production credentials.
- For local testing, prefer running a local MongoDB instance (e.g. via Docker) and point `MONGO_URI` at it.

Example using Docker for a local test MongoDB:

```bash
# start a temporary MongoDB container
docker run --rm -p 27017:27017 --name filet-crochet-mongo -d mongo:6

# run the app or tests (no need to set MONGO_URI if you want to use the default)
./gradlew bootRun
```

## Troubleshooting

- If Spring fails on startup with a placeholder resolution error for `MONGO_URI`, ensure you either set the environment variable to a valid Mongo URI or let the default be used (don't set it to an empty string).
- If tests fail with Mockito "UnnecessaryStubbingException", it usually means a test configured a mock behavior that the code under test did not exercise. The repository's tests were updated to use `lenient()` for certain stubs to avoid failing when a particular code path isn't executed.

If you want, I can add a small `.env.example` and a `docs/` note describing how to run integration tests with a temporary MongoDB container.

