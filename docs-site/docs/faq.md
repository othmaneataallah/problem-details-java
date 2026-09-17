# FAQ

## Which modules do I need?

`problem-details-core` always. Then add by output: `problem-details-jackson` for JSON, `problem-details-xml` for XML, one or both framework modules if you run Spring or Jakarta REST, `problem-details-registry` if you want reusable problem types. Nothing pulls anything you didn't ask for — the core stays dependency-free.

## Why did my XML number come back as a string?

The XML format carries no types: `<balance>30</balance>` is text on the wire, so it reads back as `"30"`. This is inherent to RFC 9457, Appendix B, and the library deliberately never guesses — guessing would corrupt values like `"01234"`. If you need typed round-trips, use JSON.

## Why is the error status 500 when my problem has no status?

An HTTP error response must carry a status code, but a problem detail may legitimately have none (outside an HTTP context, for example). Both framework integrations fall back to 500 Internal Server Error — and say so in their Javadoc. Set an explicit `status` if 500 misrepresents your error. Note the JAX-RS mapper keeps the body faithful (no status member), while Spring's rendering materializes the 500 into its own body object.

## Why is `instance` filled in even though I never set it?

That's Spring, not this library: Spring defaults an absent `instance` from the request path. Set one explicitly and it passes through untouched.

## Where do I put problem-specific data?

In typed extensions ([Core concepts](core.md#typed-extension-members)), and check the [Registry](registry.md) before inventing a new problem type. Never parse `detail` for machine-readable data — the RFC (Section 3.1.4) and this library agree: that's what extensions are for.

## Can I use this with Jackson 2 / Spring Boot 3 / Java 11?

Java 17 is the floor — no. Jackson 2 and Boot 3 are end-of-life lines; the library targets their successors (Jackson 3, Spring 7). See [Compatibility](compatibility.md).

## Is it thread-safe?

Built problems, keys, types, and registries: yes, share freely. Builders: no — build on one thread, then share the result. `JsonMapper` is thread-safe once configured; `ProblemXml` is stateless.
