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

## Households

Every pattern belongs to a household from household-api (`household-api-sb`), shared with the other
home apps. Each request names one in `X-Household-Id`; `home-household-spring` (from mavenLocal:
`./gradlew publishToMavenLocal` in that repo) checks the signed-in member belongs to it in
household-api's `HOUSEHOLD` database: `428` without the header, `403` for a household they're not in.

- Patterns carry `householdId`; every query includes it, so another household's pattern is a 404.
- Progress belongs to a household through its pattern (same `_id`): every progress call checks the
  pattern first.
- `ApplicationConfiguration` defines the one `MongoClient`, from `MONGO_APP_URL`. The household
  check uses that bean; without it Spring Boot would add its own, pointed at localhost.
- `CrochetPurger` deletes a household's patterns and progress 30 days after it's deleted in Home.
  `PATTERNS_BCK` and `PROGRESS_BCK` are manual backups and are never touched.
- `HouseholdScopingTests` checks all of that on MongoDB in Testcontainers (needs Docker).
- `scripts/backfill-household.js` gave the patterns from before households to "My household".

The `local` profile signs every request in as `LOCAL_MEMBER_EMAIL`, who needs a household in a local
household-api (port 9020) like anyone else.

## Quick start
Prerequisites: Java 25 (the Gradle toolchain) — the Gradle wrapper is
included, no local Gradle install needed.

```bash
./gradlew test       # run tests
./gradlew bootRun    # run the application
```

## Configuration
Environment variables, with local fallbacks in
`src/main/resources/application.properties`: `MONGO_APP_URL`, `MONGO_DATABASE`, `CORS_ORIGINS`,
`SERVER_PORT` (9010 locally, 9002 in the Dockerfile), `HOUSEHOLD_DATABASE` (default `HOUSEHOLD`),
and `ACCESS_TEAM_DOMAIN` / `ACCESS_AUD` for Cloudflare Access (not needed with the `local` profile).
