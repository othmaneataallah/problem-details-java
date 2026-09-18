# problem-details-java

Standard error responses for Java APIs — one model, JSON and XML output, Spring and Jakarta REST support.

If your API returns errors (it does), this library makes them consistent: every error carries the same five fields defined by [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), so clients only learn one format. New here? The [Quickstart](quickstart.md) gets you there in five minutes.

## Pick your path

- **First time?** [Quickstart](quickstart.md), then [Core concepts](core.md).
- **Sending JSON?** [JSON](json.md). **Sending XML?** [XML](xml.md).
- **Using Spring?** [Spring](spring.md). **Using Jakarta REST?** [JAX-RS](jaxrs.md).
- **Reusing error types?** [Registry](registry.md).
- **Running it?** [Demos](demos.md) — two working apps you can start in a minute.
- **Looking something up?** [API reference](api.md), [Compatibility](compatibility.md), [FAQ](faq.md).

## At a glance

Works on **Java 17 and newer**. The core has no dependencies at all; each extra piece (JSON, Spring, …) is a module you add only if you need it.

Licensed under the Apache License 2.0 — see [LICENSE](https://github.com/othmaneataallah/problem-details-java/blob/main/LICENSE).
