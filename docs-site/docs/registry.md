# Registry

Artifact: `problem-details-registry`. Before inventing an error type, check whether a reusable one already exists — that's what this module is for.

## Look one up

```java
ProblemTypeRegistry registry = new ProblemTypeRegistry(); // starts with about:blank
Optional<ProblemType> known = registry.lookup("https://example.com/probs/out-of-credit");
```

For the built-in ones there's an even shorter path:

```java
ProblemTypes.lookup("about:blank");
```

## Add your own

Nothing suitable? Mint a stable URI under your own control, give it a title and a status, point at your docs — and register it so the rest of your code finds it in one place:

```java
registry.register(
    ProblemType.of("https://example.com/probs/out-of-credit",
        "You do not have enough credit.", 403, "https://example.com/docs/probs"));
```

Each entry holds the type URI, a short title, the recommended status (empty when there is none, like `about:blank`), and a reference to wherever it's documented.

Three things the registry guarantees so you can share it across your app:

- **Typos can't shadow entries.** Registering an already-known URI fails loudly instead of quietly replacing something — possibly someone else's standard type.
- **Registries don't leak into each other.** Each instance is independent, and the shared `ProblemTypes` lookup is read-only.
- **Threads are fine.** Lookups and registrations can race safely.

One subtlety about `about:blank`: its registered title, "See HTTP Status Code", is an instruction, not a title. When you use that type, set the title to your response's status phrase (`Not Found` for a 404, in the client's language if you like) — that's what the RFC asks for.
