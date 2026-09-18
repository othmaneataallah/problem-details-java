# Spring Boot demo

A runnable consumer of `problem-details-java` **as released on Maven Central** (version pinned in `pom.xml` via `problem-details.version`). It lives outside the library's Maven reactor on purpose: it builds and runs exactly like a user project.

## Run it

```sh
cd demo/spring-boot
./../../mvnw spring-boot:run
```

The app starts on <http://localhost:8080>. Endpoints, one per case:

| Endpoint              | Case                                              |
|-----------------------|---------------------------------------------------|
| `GET /demo/ok`        | Control case, no problem                          |
| `GET /demo/credit`    | Full problem + typed extension (403)              |
| `GET /demo/missing`   | No explicit instance → defaulted from path (404)  |
| `GET /demo/boom`      | No status member → 500 fallback                   |
| `GET /demo/validate?age=-5` | Validation-style `errors` extension (422)  |
| `GET /demo/validate?age=30` | Valid input (200)                           |
| `GET /demo/types`     | The registry itself, as JSON                      |

## Verify it

With the app running, in another shell:

```sh
./verify.sh
```

The script curls every endpoint and asserts HTTP status, `application/problem+json` content type, and key body fields. It exits non-zero on the first mismatch.
