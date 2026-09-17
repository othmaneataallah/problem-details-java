# Core concepts

Artifact: `problem-details-core`. No runtime dependencies, no framework — plain Java 17 and up.

## The five members

The model uses the RFC's names exactly. Nothing is renamed, nothing is added:

| Member     | Type      | Meaning                                                        | If absent                |
|------------|-----------|----------------------------------------------------------------|--------------------------|
| `type`     | URI       | Identifies the problem *type* (the reusable part)              | `about:blank` (default)  |
| `title`    | String    | Short, human-readable summary of the type                      | omitted                  |
| `status`   | Integer   | HTTP status code for this occurrence (advisory — the real status travels on the HTTP response line) | omitted |
| `detail`   | String    | Human-readable explanation of *this occurrence* (never parsed by machines) | omitted |
| `instance` | URI       | Identifies *this occurrence*                                   | omitted                  |

A useful mental split: `type` + `title` describe the *kind* of problem and stay stable across occurrences (except for localization); `status` + `detail` + `instance` describe *this time it happened*.

`type` and `instance` accept plain strings too — `builder.type("https://example.com/probs/x")` parses them as URI references, including relative ones like `/types/out-of-credit`.

## Typed extension members

APIs usually need problem-specific data: a balance, a list of validation errors. Instead of an untyped map, each member gets a key that binds its name to its Java type:

```java
ProblemDetailKey<Integer> BALANCE = ProblemDetailKey.of("balance", Integer.class);

ProblemDetail problem = ProblemDetail.builder()
    .extension(BALANCE, 30)
    .build();

int balance = problem.get(BALANCE).orElseThrow(); // typed, no casts
```

Keys compare by name — the name is what identifies the member on the wire — so putting two values under one name keeps the last one. Lookups return `Optional`, and `has(key)` tests presence. Serializers see a read-only name-to-value view; application code should always use the typed API.

## Immutability

Built problems never change: the builder copies everything at `build()` time, exposed maps are unmodifiable, and there are no setters. Builders themselves are single-threaded by design — create them, build, and share the result freely.

## Throwing problems

`ProblemDetailException` is an unchecked exception carrying a problem detail:

```java
throw new ProblemDetailException(problem);
```

Its message is derived for plain stack traces (`detail`, else `title`, else `type`), but handlers should read `getProblemDetail()` — never parse the message. Both framework integrations map this exception to error responses automatically.
