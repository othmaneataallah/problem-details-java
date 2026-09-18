# problem-details-java

[![Build](https://github.com/othmaneataallah/problem-details-java/actions/workflows/build.yml/badge.svg)](https://github.com/othmaneataallah/problem-details-java/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.othmaneataallah/problem-details-core)](https://central.sonatype.com/search?q=g%3Aio.github.othmaneataallah)
[![License](https://img.shields.io/github/license/othmaneataallah/problem-details-java)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)

Ever returned an error from a Java API and wished every service did it the same way? [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) defines that way — a standard `type`, `title`, `status`, `detail`, and `instance` for HTTP errors. This library implements it for Java, so your APIs speak the same error language as everyone else's.

```java
throw new ProblemDetailException(
    ProblemDetail.builder()
        .type("https://example.com/probs/out-of-credit")
        .title("You do not have enough credit.")
        .status(403)
        .detail("Your current balance is 30, but that costs 50.")
        .build());
```

Your clients get a proper `application/problem+json` response — no custom error formats to invent, document, or debug.

## Start here

```xml
<dependency>
  <groupId>io.github.othmaneataallah</groupId>
  <artifactId>problem-details-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

Then follow the [Quickstart](https://othmaneataallah.github.io/problem-details-java/quickstart/) — five minutes from empty project to first error response. Full guides live on the [documentation site](https://othmaneataallah.github.io/problem-details-java/).

## What's inside

| Module | What it does |
|---|---|
| `problem-details-core` | The model itself. No dependencies, no framework needed. |
| `problem-details-jackson` | Reads and writes `application/problem+json` (Jackson 3). |
| `problem-details-xml` | Reads and writes `application/problem+xml` (JDK only, nothing extra to install). |
| `problem-details-registry` | Reusable, documented error types instead of inventing your own. |
| `problem-details-spring` | One annotation and Spring MVC/WebFlux apps render errors correctly. |
| `problem-details-jaxrs` | Same for Jakarta REST, in JSON and XML. |

Custom fields (balances, validation errors, anything yours) are first-class and type-safe — see [Core concepts](https://othmaneataallah.github.io/problem-details-java/core/).

## See it running

Two runnable demos ship in [`demo/`](demo/) — real apps using the released library, each with a script that checks every endpoint:

- [`demo/spring-boot`](demo/spring-boot) — Spring Boot app: full errors, validation lists, registry use, status fallbacks.
- [`demo/jersey`](demo/jersey) — Jersey/Grizzly app: JSON *and* XML responses from the same code.

```sh
cd demo/spring-boot
../../mvnw spring-boot:run   # in one shell
./verify.sh                  # in another — all checks should pass
```

## Reference

- [Documentation site](https://othmaneataallah.github.io/problem-details-java/) — guides, compatibility notes, FAQ.
- [API docs (Javadoc)](https://javadoc.io/doc/io.github.othmaneataallah/problem-details-core) — per module: `problem-details-core`, `problem-details-jackson`, `problem-details-xml`, `problem-details-registry`, `problem-details-spring`, `problem-details-jaxrs`.
- [Changelog](CHANGELOG.md).

## Building it yourself

You need JDK 25 to build (the code itself runs on Java 17+):

```sh
./mvnw clean verify
```

Want to help? Read [CONTRIBUTING.md](CONTRIBUTING.md) — bug reports with reproductions are the most valuable contribution.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
