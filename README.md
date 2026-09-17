# problem-details

A framework-agnostic Java implementation of [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), *Problem Details for HTTP APIs*.

The project aims to provide a lightweight and extensible implementation of Problem Details, with a dependency-free core and optional integrations for JSON, XML, Spring, and JAX-RS.

## Status

Work in progress.

The project is currently being developed from the ground up, starting with the core RFC 9457 model.

## Planned modules

- `problem-details-core` — dependency-free Problem Details model and API
- `problem-details-jackson` — JSON serialization (`application/problem+json`)
- `problem-details-xml` — XML serialization (`application/problem+xml`)
- `problem-details-registry` — IANA and custom problem-type registry
- `problem-details-spring` — Spring MVC/WebFlux integration
- `problem-details-jaxrs` — JAX-RS integration

## Requirements

- Java 17+
- Maven

The project is developed using a newer JDK, but published artifacts target Java 17.

## License

License information will be added before the first release.