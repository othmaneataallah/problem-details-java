# JAX-RS

Artifact: `problem-details-jaxrs` (Jakarta REST 4, API-only dependency — your runtime provides the rest). Two standard providers; register them on your application however your implementation likes (for example, an `Application` subclass).

## Returning problems

Return a problem from a resource and it negotiates like any other entity — JSON or XML depending on what the client asked for:

```java
@GET
@Produces({"application/problem+json", "application/problem+xml"})
public ProblemDetail get() {
    return problem;
}
```

## Throwing problems

Thrown problems always answer JSON, so error responses stay predictable no matter what the client accepts:

```java
throw new ProblemDetailException(problem);
```

As with Spring, a problem without `status` answers 500 — with the body left exactly as you built it.
