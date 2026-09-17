# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [Unreleased]

## [0.1.0] - 2026-09-17

### Added

- `problem-details-core`: immutable RFC 9457 model — `ProblemDetail` with builder,
  typed extension keys (`ProblemDetailKey<T>`), and `ProblemDetailException`.
  Zero runtime dependencies.
- `problem-details-jackson`: JSON serialization (`application/problem+json`) on
  Jackson 3, with lenient RFC-mandated reading.
- `problem-details-xml`: XML serialization (`application/problem+xml`, RFC 9457
  Appendix B) using only the JDK.
- `problem-details-registry`: IANA problem-type constants and helpers,
  thread-safe `ProblemTypeRegistry` with custom-type support.
- `problem-details-spring`: thin Spring MVC/WebFlux integration on Spring 7 —
  converter plus a stack-agnostic `@ControllerAdvice`.
- `problem-details-jaxrs`: JAX-RS integration on Jakarta REST 4 —
  `ExceptionMapper` and `MessageBodyWriter` for both media types.
