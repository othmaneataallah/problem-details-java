# problem-details-java

A framework-agnostic Java implementation of [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), *Problem Details for HTTP APIs*.

The project provides a lightweight and extensible implementation with a dependency-free core and optional modules for JSON and XML serialization, a problem-type registry, and Spring and JAX-RS integrations.

## Status

All planned modules are implemented on `main`. The project versions as `0.1.0-SNAPSHOT` and has **not** been published to Maven Central yet; publishing is deliberately deferred until the codebase and testing are finalized.

## Modules

| Artifact                          | Description                                                                                   |
|-----------------------------------|-----------------------------------------------------------------------------------------------|
| `problem-details-core`            | Dependency-free problem details model and API: immutable `ProblemDetail` + builder, typed extension keys (`ProblemDetailKey<T>`), `ProblemDetailException` |
| `problem-details-jackson`         | JSON serialization (`application/problem+json`) on Jackson 3                                   |
| `problem-details-xml`             | XML serialization (`application/problem+xml`, RFC 9457 Appendix B) using only the JDK         |
| `problem-details-registry`        | IANA problem-type constants/helpers, `ProblemTypeRegistry`, custom problem-type support       |
| `problem-details-spring`          | Thin Spring MVC/WebFlux integration (Spring 7): converter plus a stack-agnostic `@ControllerAdvice` |
| `problem-details-jaxrs`           | JAX-RS integration (Jakarta REST 4): `ExceptionMapper` and `MessageBodyWriter` for both media types |

Group ID for all artifacts: `io.github.othmaneataallah`.

## Usage

Core model — no dependencies beyond the core artifact:

```java
ProblemDetail problem = ProblemDetail.builder()
    .type("https://example.com/probs/out-of-credit")
    .title("You do not have enough credit.")
    .status(403)
    .detail("Your current balance is 30, but that costs 50.")
    .build();
```

JSON with Jackson 3:

```java
JsonMapper mapper = JsonMapper.builder()
    .addModule(new ProblemDetailsModule())
    .build();
String json = mapper.writeValueAsString(problem); // application/problem+json
```

XML with the JDK only:

```java
String xml = ProblemXml.toXml(problem); // application/problem+xml
```

Spring (register the advice explicitly, e.g. `@Import(ProblemDetailAdvice.class)`):

```java
throw new ProblemDetailException(problem); // rendered as application/problem+json
```

See each module's `package-info.java` and the Javadoc for details, including extension members, the registry, and leniency rules.

## Requirements

To **use** the library: Java 17+.

To **build** it:

- JDK 25 (pinned in `.sdkmanrc`; artifacts still target Java 17)
- Maven via the wrapper: `./mvnw clean verify`

## Building and testing

```sh
./mvnw clean verify   # full build, tests, and format check (JDK 21+)
./mvnw spotless:apply # auto-fix formatting (google-java-format)
```

CI runs the same verification on JDK 17 and JDK 25. Java formatting is enforced by Spotless; see `AGENTS.md` for contributor conventions.

## IntelliJ IDEA setup

No committed run configurations are needed (this is a library, not an application):

1. Open the project and set the Project SDK to JDK 25.
2. Under *Settings → Build Tools → Maven*, use the Maven wrapper.
3. Run the build from the Maven tool window (`clean verify`), or create personal (uncommitted) Maven run configurations for `clean verify` and `spotless:apply`.
4. Tests also run natively (right-click a test or class). For exact Google Java Style formatting, install the google-java-format plugin matching the version pinned in the root `pom.xml`, or run `./mvnw spotless:apply` before committing.

## License

Licensed under the Apache License, Version 2.0 — see [LICENSE](LICENSE).
