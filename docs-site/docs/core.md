# Core concepts

Artifact: `problem-details-core`. No dependencies, no framework — plain Java 17 and up. Everything else in this library builds on what's here.

## The five fields

Errors have the same five fields everywhere, with the RFC's names:

| Field      | Example                                          | Notes                                                        |
|------------|--------------------------------------------------|--------------------------------------------------------------|
| `type`     | `https://example.com/probs/out-of-credit`        | *What kind* of problem. A URI you control. Defaults to `about:blank`. |
| `title`    | `You do not have enough credit.`                 | Short summary. Same for every occurrence (translations aside). |
| `status`   | `403`                                            | The HTTP status. Handy to have inside the body, but the response line stays authoritative. |
| `detail`   | `Your current balance is 30, but that costs 50.` | What happened *this time*. Written for humans, never parsed by machines. |
| `instance` | `/account/12345/msgs/abc`                        | Which occurrence this is.                                  |

Think of it as two halves: `type` + `title` describe the *kind* of problem (stable, reusable), while `status` + `detail` + `instance` describe *this time it happened*.

`type` and `instance` accept plain strings — relative references like `/types/out-of-credit` work fine.

## Your own fields

Real errors carry their own data. Instead of a loose map, each field gets a key that pairs its name with its Java type, so reads are checked by the compiler:

```java
ProblemDetailKey<Integer> BALANCE = ProblemDetailKey.of("balance", Integer.class);

ProblemDetail problem = ProblemDetail.builder()
    .extension(BALANCE, 30)
    .build();

int balance = problem.get(BALANCE).orElseThrow(); // no casts, ever
```

Missing fields come back empty (`Optional`), `has(key)` tests presence, and putting two values under one name keeps the last — the name is what identifies the field wherever it travels.

## Built problems never change

Once built, a problem is frozen: no setters, and everything you read out is unmodifiable. Builders are single-use in spirit — build on one thread, share the result on all of them.

## Throwing problems

`ProblemDetailException` is an unchecked exception that carries the whole structured error:

```java
throw new ProblemDetailException(problem);
```

Handlers read `getProblemDetail()` — the exception message is only there so stack traces stay readable. The [Spring](spring.md) and [JAX-RS](jaxrs.md) integrations turn these exceptions into HTTP responses for you.
