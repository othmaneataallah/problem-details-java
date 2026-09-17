# problem-details-java

A framework-agnostic Java implementation of [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), *Problem Details for HTTP APIs* — with a dependency-free core and optional modules for JSON and XML serialization, a problem-type registry, and Spring and JAX-RS integrations.

Use the tabs above to find your path. New here? Start with [Quickstart](quickstart.md) — you'll return your first problem response in about five minutes.

## What you get

- One immutable model for all five RFC members, with a builder — [Core concepts](core.md).
- Typed extension members instead of stringly-typed maps.
- JSON (`application/problem+json`) on Jackson 3 and XML (`application/problem+xml`) with no third-party dependencies — [JSON](json.md), [XML](xml.md).
- A problem-type registry for reuse instead of reinvention — [Registry](registry.md).
- One-line integrations for Spring MVC/WebFlux and Jakarta REST — [Spring](spring.md), [JAX-RS](jaxrs.md).

## Requirements

To **use** the library: Java 17 or newer. See [Compatibility](compatibility.md) for the exact dependency versions each module expects.

## License

Licensed under the Apache License, Version 2.0 — see [LICENSE](https://github.com/othmaneataallah/problem-details-java/blob/main/LICENSE). Something unclear? The [FAQ](faq.md) probably answers it.
