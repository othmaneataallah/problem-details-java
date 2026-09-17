# Quickstart

Add the core artifact (group ID `io.github.othmaneataallah`) and build problem details with the immutable model:

```java
ProblemDetail problem = ProblemDetail.builder()
    .type("https://example.com/probs/out-of-credit")
    .title("You do not have enough credit.")
    .status(403)
    .detail("Your current balance is 30, but that costs 50.")
    .build();
```

The five standard members follow RFC 9457 naming exactly: `type`, `title`, `status`, `detail`, `instance`. All are optional; an unset `type` defaults to `about:blank`, as the RFC prescribes.

## Typed extension members

Problem-type-specific members use typed keys instead of an untyped map:

```java
ProblemDetailKey<Integer> BALANCE = ProblemDetailKey.of("balance", Integer.class);

ProblemDetail problem = ProblemDetail.builder()
    .extension(BALANCE, 30)
    .build();

int balance = problem.get(BALANCE).orElseThrow();
```

## Throwing problems

```java
throw new ProblemDetailException(problem);
```

The exception carries the structured detail, so handlers — including the [Spring](spring.md) and [JAX-RS](jaxrs.md) integrations — never parse message strings. Serialize it with the [JSON](json.md) or [XML](xml.md) modules.
