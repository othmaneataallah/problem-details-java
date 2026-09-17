# Spring

Artifact: `problem-details-spring` (Spring Framework 7, `spring-web` only). Spring already renders RFC 9457 responses natively — this module bridges your immutable model into that pipeline instead of building a parallel one.

## Setup

Register the single advice explicitly. Component scanning doesn't reach into library packages, and there is deliberately no Boot auto-configuration keeping this thin:

```java
@Import(ProblemDetailAdvice.class)
```

The advice uses only `spring-web` types, so the same class serves **Spring MVC and Spring WebFlux** applications.

## Usage

Throw as usual — the advice maps the exception to an `application/problem+json` response:

```java
throw new ProblemDetailException(problem);
```

What happens on the way out:

- The response status comes from the problem detail, or **500** when it has no `status` member (an error response must carry a status; the RFC defines none for status-less problems).
- Your typed extensions are copied into Spring's properties map for rendering.
- An absent `instance` is defaulted from the request path by Spring; an explicit one passes through untouched.

The `SpringProblemDetails` helper exposes the same conversion (`toSpringDetail`, `toResponseEntity`, `statusCode`) for handlers you write yourself.
