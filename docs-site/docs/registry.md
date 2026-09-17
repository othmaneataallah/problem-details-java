# Registry

Artifact: `problem-details-registry`. Before inventing a problem type, check whether a reusable one exists — that's what the "HTTP Problem Types" registry of RFC 9457, Section 4.2 is for, and what this module models.

## Look up first

```java
ProblemTypeRegistry registry = new ProblemTypeRegistry(); // preloaded with about:blank
Optional<ProblemType> known = registry.lookup("https://example.com/probs/out-of-credit");
```

One-liner access to the spec-registered types (today, just `about:blank`) lives on `ProblemTypes`:

```java
ProblemTypes.lookup("about:blank"); // Optional[ProblemType]
```

## Register your own

If nothing fits, mint a stable URI under your control, document it (type URI, title, status code — the RFC's required trio, Section 4), and register it:

```java
registry.register(
    ProblemType.of("https://example.com/probs/out-of-credit",
        "You do not have enough credit.", 403, "https://example.com/docs/probs"));
```

Each entry carries its type URI, title, recommended status (empty when a type has no recommendation, like `about:blank`), and a reference to its defining specification.

Three guarantees make registries safe to share:

- **Duplicates fail fast.** Registering an already-known URI throws instead of silently shadowing an entry — possibly someone else's IANA one.
- **Instances are independent.** Registering on one registry never affects another, and the shared `ProblemTypes` lookup is read-only.
- **Thread-safe.** Lookups and registrations can race; exactly one duplicate registration wins.

A note on `about:blank`: its registered title, "See HTTP Status Code", is not a usable title. When you use that type, set the title to the status phrase of your response (`Not Found` for 404, localized if you like) — that's what the RFC asks for.
