@README.md

## Part of the home stack

This is the API for one of five home apps served at `home.ksl-apps.dev`. The stack (nginx,
Cloudflare Tunnel, every API) lives in `~/WebstormProjects/home-server`; read its `README.md` for
the full picture.

- Reached at `/filet-crochet-api/`, behind Cloudflare Access. Container `backend-filet-crochet`,
  config from `home-server/home/env/filet-crochet.env`.
- UI repo: `~/WebstormProjects/filet-crochet-grid`. Its Koa `backend/` folder is an older
  implementation of this API; production runs this one. A UI change belongs in that repo.
- Households: `home-household-spring` (from mavenLocal) checks `X-Household-Id` against
  household-api's `HOUSEHOLD` database. A change to that check belongs in
  `~/IdeaProjects/home-household-spring`; a change to households themselves in
  `~/IdeaProjects/household-api-sb`.
- Deploy from `home-server`: `make deploy-filet-crochet` builds this jar and the UI, recreates the
  container, waits for it to be healthy and reloads nginx (`make filet-crochet` builds only the jar).
  The build skips tests (`-x test`), so run `./gradlew test` yourself first.

## Working here

- Gradle wrapper: `./gradlew build`, `./gradlew test`. Java 25, Spring Boot 4.1.
- Commit and push straight to `main`. Messages are plain sentences describing the change
  (`Keep each household's patterns separate`).
