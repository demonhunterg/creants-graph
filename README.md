# creants-graph

Account & authentication service for Creants — Spring Boot REST API providing user signup, login (username/password, Facebook, Google, guest), password recovery via email verify-code, and JWT-based session management.

## Tech Stack

| Component   | Technology                          |
| ----------- | ----------------------------------- |
| Language    | Java 8 (builds on JDK 11)           |
| Framework   | Spring Boot 2.5.6                   |
| Build       | Gradle 7.6.4 (wrapper included)     |
| Datastore   | MongoDB (accounts), Redis (cache)   |
| Auth        | JWT (`java-jwt`), Spring Security   |
| API Docs    | Swagger / Springfox 2.9.2           |
| Logging     | Log4j2                            |

## Prerequisites

- JDK 8 or 11
- MongoDB running on `localhost:27017`
- Redis running on `localhost:6379`

macOS quick setup:

```bash
brew install redis
brew tap mongodb/brew && brew install mongodb-community
brew services start redis
brew services start mongodb-community
```

## Getting Started

```bash
./gradlew bootRun
```

The API listens on **http://localhost:9393**.

- Swagger UI: http://localhost:9393/swagger-ui.html
- Health check: http://localhost:9393/actuator/health

> `actuator/health` reports `DOWN` on machines that cannot reach the internal
> mail server (`mail.creants.net`) — the mail health indicator only affects the
> aggregate status, not the API itself.

## Configuration

All settings live in `src/main/resources/application.properties`:

| Key                                              | Description                        |
| ------------------------------------------------ | ---------------------------------- |
| `server.port`                                    | HTTP port (default `9393`)         |
| `spring.data.mongodb.*`                          | MongoDB connection                 |
| `spring.redis.host` / `port` / `password`        | Redis connection                   |
| `spring.mail.*`                                  | SMTP for password-recovery emails  |
| `fb.app.id` / `fb.app.secret`                    | Facebook OAuth app credentials     |

> Never commit real credentials — use environment overrides
> (e.g. `SPRING_DATA_MONGODB_HOST=...`) for non-local environments.

## API Overview

| Method | Path                    | Description                          |
| ------ | ----------------------- | ------------------------------------ |
| POST   | `/account/signup`       | Register a new account               |
| POST   | `/account/recovery`     | Email a password-reset verify code   |
| POST   | `/account/recovery/verify` | Check a verify code               |
| POST   | `/account/recovery/reset`  | Reset password with verify code   |
| POST   | `/oauth/creants`        | Login with username/password         |
| POST   | `/oauth/fb`             | Login with Facebook token            |
| POST   | `/oauth/gg`             | Login with Google token              |
| POST   | `/oauth/guest`          | Guest login                          |
| POST   | `/internal/verify`      | Verify JWT (internal services)       |
| GET    | `/internal/check/uptime`| Uptime check                         |

## Build

```bash
./gradlew build        # compile + test + jar
./gradlew fatBuild     # jar + web src + config + runtime libs -> build/libs
```

## Docker

```bash
./gradlew fatBuild
docker build -t creants-graph .
docker run -p 9393:9393 creants-graph
```

## Project Structure

```
src/main/java/com/creants/graph/
├── config/       # Swagger, web-security configuration
├── controller/   # REST endpoints (account, oauth, internal)
├── dao/          # MongoDB repositories
├── om/           # Domain objects
├── security/     # JWT filter, provider, models
├── service/      # CacheService (Redis), AccountManager, MailService...
└── util/         # Configs, Security, Tracer helpers
```

## Running on Devin

`environment.yaml` at the repo root is the snapshot-setup blueprint — it
installs JDK 11, Redis and MongoDB, then warms the Gradle cache.
