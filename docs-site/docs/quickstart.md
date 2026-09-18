# Quickstart

From empty project to your first standard error response in five minutes.

## 1. Add two dependencies

Maven:

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

Gradle:

```groovy
implementation "io.github.othmaneataallah:problem-details-core:0.1.0"
implementation "io.github.othmaneataallah:problem-details-jackson:0.1.0"
```

## 2. Describe an error

```java
ProblemDetail problem = ProblemDetail.builder()
    .type("https://example.com/probs/out-of-credit")
    .title("You do not have enough credit.")
    .status(403)
    .detail("Your current balance is 30, but that costs 50.")
    .build();
```

Every field is optional. Skip `type` and it becomes `about:blank` — a blank problem with no special meaning, which is exactly what you want for one-off errors. [What each field means →](core.md)

## 3. Send it as JSON

```java
JsonMapper mapper = JsonMapper.builder()
    .addModule(new ProblemDetailsModule())
    .build();

String json = mapper.writeValueAsString(problem);
```

Your client receives `application/problem+json`:

```json
{
  "type": "https://example.com/probs/out-of-credit",
  "title": "You do not have enough credit.",
  "status": 403,
  "detail": "Your current balance is 30, but that costs 50."
}
```

## 4. Throw it in a real app

```java
throw new ProblemDetailException(problem);
```

In Spring, one registered advice turns that into the HTTP response. In Jakarta REST, two registered providers do the same. No message parsing anywhere — the structured error travels with the exception.

## What next?

- Your errors carry extra data (balances, field errors)? [Core concepts](core.md) shows how to add typed fields.
- Reusing an error type instead of inventing one? [Registry](registry.md).
- XML instead of JSON? [XML](xml.md).
- Prefer learning by running? [Demos](demos.md).
