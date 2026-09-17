# problem-details-java

A framework-agnostic Java implementation of [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), *Problem Details for HTTP APIs* — with a dependency-free core and optional modules for JSON and XML serialization, a problem-type registry, and Spring and JAX-RS integrations.

Group ID for all artifacts: `io.github.othmaneataallah`. Current version: `0.1.0-SNAPSHOT` (not yet published to Maven Central).

## Modules

- [Quickstart](quickstart.md) — core model in five minutes
- [JSON](json.md) — `application/problem+json` on Jackson 3
- [XML](xml.md) — `application/problem+xml` on the JDK only
- [Registry](registry.md) — IANA and custom problem types
- [Spring](spring.md) — Spring MVC/WebFlux integration
- [JAX-RS](jaxrs.md) — Jakarta REST integration

## Requirements

To **use** the library: Java 17+.

To **build** it: JDK 25 (pinned in `.sdkmanrc`; artifacts target Java 17) and the Maven wrapper:

```sh
./mvnw clean verify
```

## License

Licensed under the Apache License, Version 2.0 — see [LICENSE](https://github.com/othmaneataallah/problem-details-java/blob/main/LICENSE).
