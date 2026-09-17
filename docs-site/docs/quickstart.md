# Quickstart

This page takes you from an empty project to your first problem response. It uses the core module plus JSON output; every step also links to the full guide.

## 1. Add the dependencies

Maven (`pom.xml`):

```xml
<dependency>
  <groupId>io.github.othmaneataallah</groupId>
  <artifactId>problem-details-core</artifactId>
  <version>0.1.0</version>
</dependency>
<dependency>
  <groupId>io.github.othmaneataallah</groupId>
  <artifactId>problem-details-jackson</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle (`build.gradle`):

```groovy
implementation "io.github.othmaneataallah:problem-details-core:0.1.0"
implementation "io.github.othmaneataallah:problem-details-jackson:0.1.0"
```

The core has zero runtime dependencies and works with no web framework at all.

## 2. Build a problem

```java
ProblemDetail problem = ProblemDetail.builder()
    .type("https://example.com/probs/out-of-credit")
    .title("You do not have enough credit.")
    .status(403)
    .detail("Your current balance is 30, but that costs 50.")
    .build();
```

All five members are optional. Leave `type` out and it defaults to `about:blank`, exactly as the RFC prescribes. Read [Core concepts](core.md) for what each member means.

## 3. Send it as JSON

```java
JsonMapper mapper = JsonMapper.builder()
    .addModule(new ProblemDetailsModule())
    .build();

String json = mapper.writeValueAsString(problem);
```

That produces `application/problem+json`:

```json
{
  "type": "https://example.com/probs/out-of-credit",
  "title": "You do not have enough credit.",
  "status": 403,
  "detail": "Your current balance is 30, but that costs 50."
}
```

## 4. Throw it in your app

```java
throw new ProblemDetailException(problem);
```

The exception carries the structured detail, so your handlers never parse message strings. In Spring, register `ProblemDetailAdvice` and you're done; in Jakarta REST, register the two providers. Details: [Spring](spring.md), [JAX-RS](jaxrs.md).

## Where next

- Problem-specific data (balances, validation errors)? [Core concepts](core.md#typed-extension-members), then [Registry](registry.md) to reuse a type instead of inventing one.
- XML output? [XML](xml.md).
- Something surprising? [FAQ](faq.md).
