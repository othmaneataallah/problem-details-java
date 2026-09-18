# Spring

Artifact: `problem-details-spring` (Spring Framework 7). Spring already knows how to render standard errors — this module plugs your problems into that machinery instead of building a second one.

## Setup

Register one advice class. Component scanning won't find it inside the library, so say it explicitly:

```java
@Import(ProblemDetailAdvice.class)
```

That's the whole setup. The same class works in Spring MVC and Spring WebFlux apps.

## Usage

Throw like anywhere else:

```java
throw new ProblemDetailException(problem);
```

You get an `application/problem+json` response with your problem's status, fields, and custom data. Two details:

- **No status on the problem?** The response goes out as 500 — an error response needs a status, and yours didn't name one. Prefer an explicit `status` when 500 would mislead.
- **No instance on the problem?** Spring fills it in from the request path. Set one yourself and yours wins.

The `SpringProblemDetails` helper offers the same conversion (`toSpringDetail`, `toResponseEntity`, `statusCode`) for handlers you write by hand.
