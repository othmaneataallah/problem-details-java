# Registry

Artifact: `problem-details-registry`. Models the "HTTP Problem Types" registry of RFC 9457, Section 4.2: look up reusable type URIs before minting your own (Section 4.1).

```java
ProblemTypeRegistry registry = new ProblemTypeRegistry(); // preloaded with about:blank
registry.register(
    ProblemType.of("https://example.com/probs/out-of-credit",
        "You do not have enough credit.", 403, "https://example.com/docs/probs"));

ProblemType type = registry.lookup("https://example.com/probs/out-of-credit").orElseThrow();
```

Notes:

- Every registry starts with the one spec-registered type, `about:blank` (also available as `ProblemTypes.ABOUT_BLANK`). Its registered title, "See HTTP Status Code", is not a usable title — use the status phrase of the actual response instead.
- Duplicate registrations fail fast rather than shadowing entries, and registries are independent and thread-safe.
- One-liner lookups of well-known types: `ProblemTypes.lookup(uri)`.
