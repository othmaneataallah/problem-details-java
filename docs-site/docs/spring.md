# Spring

Artifact: `problem-details-spring` (Spring Framework 7, `spring-web`). A thin bridge into Spring's native RFC 9457 support — not a parallel renderer.

Register the single stack-agnostic advice explicitly (component scanning does not reach into library packages, and no Boot auto-configuration is provided):

```java
@Import(ProblemDetailAdvice.class)
```

Then throw as usual:

```java
throw new ProblemDetailException(problem); // rendered as application/problem+json
```

Behavior:

- The response status comes from the problem detail, defaulting to 500 when it has no `status` member.
- Typed extensions are copied into Spring's properties map for rendering.
- An absent `instance` is defaulted from the request path by Spring; an explicit one passes through.
- The same advice serves Spring MVC and Spring WebFlux applications.
