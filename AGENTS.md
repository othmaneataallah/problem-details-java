# AGENTS.md

## Project

`problem-details` is an open-source Java library implementing RFC 9457, "Problem Details for HTTP APIs":

    https://www.rfc-editor.org/rfc/rfc9457

The project provides a framework-agnostic implementation with a dependency-free core and optional modules for serialization and web framework integrations.

The intended differentiation is:

1. A dependency-free core usable without any web framework.
2. JSON and XML support.
3. Typed extension members through a `ProblemDetailKey<T>` pattern rather than exposing an untyped `Map<String, Object>` as the primary API.
4. An IANA problem-type registry helper with support for custom problem types.

The core must remain framework-agnostic. Framework integrations are opt-in modules.

---

## Normative Reference

The repository contains a local copy of RFC 9457 at:

    docs/standards/rfc.txt

When implementing behavior defined by RFC 9457, consult this document  directly rather than relying on memory or assumptions about the RFC.

The RFC is the authority for RFC-defined behavior. Project-specific API design decisions must not contradict the RFC.

RFC 9457 defines the standard Problem Details members:

- `type`
- `title`
- `status`
- `detail`
- `instance`

It also permits extension members.

When implementing or testing RFC-defined behavior:

- Identify the relevant RFC section.
- Distinguish mandatory requirements from recommendations and optional behavior.
- Do not invent behavior that is presented as an RFC requirement.
- If the RFC leaves behavior unspecified, make the project decision explicit rather than pretending it is mandated by the RFC.

The local RFC copy is reference material only. It must not be packaged into any published artifact or included as a runtime dependency.

---

## Repository Structure

This is a Maven multi-module project.

The repository root is the Maven parent/aggregator and contains no Java source code.

Root Maven coordinates:

- groupId: `io.github.othmaneataallah`
- artifactId: `problem-details-java`
- version: `0.1.0-SNAPSHOT`
- packaging: `pom`

Child modules inherit the parent `groupId` and `version` and use artifactIds:

- `problem-details-core`
- `problem-details-jackson`
- `problem-details-xml`
- `problem-details-registry`
- `problem-details-spring`
- `problem-details-jaxrs`

Java packages follow the base `io.github.othmaneataallah.problemdetails`, with one subpackage per module (for example, `...problemdetails.core` for `problem-details-core`).

Do not create a `src/` directory in the repository root.

The root is responsible for project-wide build configuration and aggregation. Actual Java code belongs in child modules.

---

## Build and Development Environment

### Java

The local development JDK is Java 25.

Published artifacts must remain compatible with Java 17.

The Maven build uses:

    <maven.compiler.release>17</maven.compiler.release>

Do not change the compatibility target to Java 25 merely because the local development JDK is Java 25.

Development JDK and published compatibility are separate concerns:

    Development JDK:         25
    Published compatibility: 17

Production code must not use Java language features or Java APIs that make the library incompatible with Java 17.

### SDKMAN

The project uses SDKMAN for local development toolchain management.

`.sdkmanrc` is project configuration and should be committed.

Do not add `.sdkmanrc` to `.gitignore`.

### Maven Wrapper

The repository uses the Maven Wrapper.

Prefer:

    ./mvnw

over relying on a globally installed Maven executable.

Do not remove, replace, or regenerate the Maven Wrapper without a specific reason.

---

## Module Roadmap

Modules must be implemented in the following order.

Do not scaffold future modules before their phase is reached.

### Phase 1 — `problem-details-core`

Contains:

- `ProblemDetail` model
- builder/API for constructing problem details
- base exception type, if appropriate to the design

Runtime dependency requirements:

- Zero runtime dependencies.
- No JSON libraries.
- No XML libraries.
- No Spring dependencies.
- No JAX-RS dependencies.
- No framework-specific abstractions.

If a proposed change to `core` requires one of these dependencies, stop and flag the architectural issue rather than adding the dependency.

### Phase 2 — `problem-details-jackson`

Provides JSON serialization using Jackson.

Media type:

    application/problem+json

Depends on:

    problem-details-core

### Phase 3 — `problem-details-xml`

Provides XML serialization.

Media type:

    application/problem+xml

Depends on:

    problem-details-core

### Phase 4 — `problem-details-registry`

Contains:

- IANA problem-type constants/helpers
- `ProblemTypeRegistry`
- support for registering custom problem types

Depends on:

    problem-details-core

### Phase 5 — `problem-details-spring`

Provides thin integration with Spring MVC/WebFlux.

This module must not introduce Spring dependencies into `core`.

### Phase 6 — `problem-details-jaxrs`

Provides thin JAX-RS integration.

Potential responsibilities include:

- `ExceptionMapper`
- `MessageBodyWriter`

This module must not introduce JAX-RS dependencies into `core`.

---

## Architecture

The dependency graph must remain one-directional.

Conceptually:

    problem-details-core
       ↑       ↑       ↑
       │       │       │
    jackson    xml   registry
       ↑
       │
    framework adapters

More precisely:

- Serialization modules may depend on `core`.
- The registry may depend on `core`.
- Framework adapters may depend on appropriate lower-level modules.
- `core` must never depend on serialization libraries or web frameworks.
- Avoid circular dependencies.

### Framework Independence

`problem-details-core` must be usable from a plain Java application with
no web framework and no serialization library on the classpath.

Do not introduce framework concepts into the core API merely for convenience.

---

## Public API Design

This is a public library intended for consumption by other developers.

Prefer:

- Immutable types.
- `final` classes where appropriate.
- Records where appropriate and compatible with the Java 17 target.
- Builders for complex construction.
- Explicit and well-defined public APIs.
- Minimal accidental API surface.

Avoid mutable setters as the default public API.

Before introducing non-trivial public API, consider:

- The API's long-term compatibility implications.
- Whether the abstraction is actually necessary.
- How the API maps to RFC 9457 terminology.
- How the API will behave across serialization modules.
- Whether the API can remain stable as future modules are added.

Do not expose implementation details unnecessarily.

### Nullness Policy

Main (non-test) code must not depend on any nullness annotation library,
including JSpecify. This keeps `problem-details-core` strictly dependency-free
and keeps the published POMs of all modules free of annotation processing
concerns.

Express nullness contracts instead through Javadoc (`@param`, `@return`,
`@throws NullPointerException`) and enforce them at runtime with
`java.util.Objects.requireNonNull` where a null value would violate the API
contract.

Revisit JSpecify (currently `org.jspecify:jspecify:1.0.0`, the industry-preferred
nullness annotation set) only as a later, deliberate decision once the core API
is stable — and then only as an `optional` compile-time dependency, never as a
runtime requirement.

---

## RFC Terminology

Use RFC 9457 terminology consistently.

Prefer the exact names:

    type
    title
    status
    detail
    instance

Do not invent alternative names such as:

    errorType
    errorTitle
    httpStatus
    description
    resource

unless there is a compelling, documented reason.

The public API should make the relationship between the Java API and the RFC terminology clear.

---

## Documentation

Every public class, constructor, method, and other public API element must have appropriate Javadoc before the work is considered complete.

Javadoc should document:

- Behavior.
- Parameters.
- Return values.
- Exceptions.
- Important invariants.
- Relevant RFC semantics where appropriate.

Do not write Javadoc that merely restates the method or class name.

Documentation is part of the public API of this library.

---

## Testing

Testing is a first-class part of the implementation.

The goal is not merely to test implementation details. Tests should provide confidence that the public API behaves correctly and, where applicable, conforms to RFC 9457.

### General Testing Requirements

- Every non-trivial public behavior must have appropriate automated tests.
- Tests must be deterministic and isolated.
- Prefer unit tests for core behavior.
- Test both normal and meaningful edge cases.
- Test invalid input where the API defines meaningful behavior for it.
- Avoid tests that depend unnecessarily on implementation details.
- When behavior is defined by RFC 9457, tests should verify the relevant
  RFC requirement rather than merely verifying the current implementation.

### Core Tests

`problem-details-core` must remain dependency-free at runtime.

Its tests must not cause JSON, XML, Spring, JAX-RS, or other framework libraries to become runtime dependencies of the published core artifact.

Core behavior should be tested independently of serialization frameworks.

### Serialization Tests

JSON and XML modules should test, as applicable:

- Serialization.
- Deserialization.
- Core members.
- Extension members.
- Absent versus null values where relevant.
- Media-type-specific behavior.
- Round-trip behavior.
- RFC-defined constraints and semantics.
- Interoperability concerns.

### Test Dependencies

The standardized test stack is JUnit 6 (Jupiter) plus AssertJ. Versions are
managed centrally in the parent POM (`org.junit:junit-bom` import plus an
`assertj-core` version property); child modules must not declare their own
versions.

A test-scoped dependency may be used when appropriate.

However, test dependencies must not accidentally become runtime dependencies of the published module.

### Completion Requirement

A change is not considered complete merely because it compiles.

Before considering implementation work complete, run:

    ./mvnw clean verify

and ensure the relevant Maven reactor and tests pass.

---

## Code Formatting

Java formatting is enforced by Spotless with google-java-format. Versions are
pinned in the parent POM (`spotless.version`,
`google-java-format.version`); do not declare them per module.

- Fix formatting with `./mvnw spotless:apply`.
- Verify with `./mvnw spotless:check`.
- `spotless:check` also runs automatically as part of `./mvnw clean verify`
  on JDK 21 and newer (profile `format-check`).

google-java-format requires JDK 21+ to run, so builds on older JDKs (for
example the Java 17 compatibility leg in CI) skip the format check but still
compile and test normally.

For IntelliJ IDEA: `.editorconfig` covers the basics natively. For exact
Google Java Style, run `./mvnw spotless:apply` before committing or install
the google-java-format IntelliJ plugin matching the pinned version. Do not
commit `.idea/` code-style files; Spotless output is the source of truth.

---

## License

The project is licensed under the Apache License, Version 2.0 (see `LICENSE`
at the repository root, SPDX identifier `Apache-2.0`). The parent POM
`<licenses>` block declares the same license. Keep them consistent.

---

## Git and Repository Conventions

The repository contains:

- `.gitignore` for generated and IDE-specific files.
- `.gitattributes` for consistent Git text handling.
- `.sdkmanrc` for development toolchain configuration.
- Maven Wrapper files for reproducible Maven builds.
- `docs/standards/rfc.txt` as the local RFC reference.

Do not commit:

- `.idea/`
- Maven `target/` directories.
- Compiled `.class` files.
- IDE-specific project files.
- Generated build artifacts.

Do not add generated artifacts to the source repository.

---

## Development Workflow

Before making implementation changes:

1. Inspect the existing repository and current Git state.
2. Read this `AGENTS.md`.
3. Inspect the relevant Maven POM(s).
4. Inspect relevant existing source and test code before modifying it.
5. Consult `docs/standards/rfc.txt` when implementing RFC-defined
   behavior.
6. Do not assume planned modules already exist.
7. Do not create future modules ahead of their phase.
8. Do not recreate or overwrite existing project infrastructure without
   a reason.
9. Keep changes limited to the current development phase.
10. Add or update tests alongside implementation changes.
11. Run the appropriate Maven verification commands after changes.

Use the Maven Wrapper:

    ./mvnw clean verify

When behavior or API design is non-trivial, explain the architectural
reasoning before making the change.

---

## Current Development Phase

The project is currently **before Phase 1**.

The repository infrastructure has been initialized, but library implementation has not started.

Currently, there are:

- No child Maven modules.
- No `problem-details-core`.
- No `ProblemDetail` implementation.
- No Jackson module.
- No XML module.
- No registry module.
- No Spring module.
- No JAX-RS module.

Do not create any implementation module unless the current task explicitly starts its corresponding phase.

The first implementation phase is `problem-details-core`.
