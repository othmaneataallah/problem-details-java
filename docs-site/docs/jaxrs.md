# JAX-RS

Artifact: `problem-details-jaxrs` (Jakarta REST 4 — the API dependency is `provided` scope, so your runtime supplies it). Two standard providers; register them on your application however your implementation prefers (e.g. `Application` subclass):

- `ProblemDetailMessageBodyWriter` serializes problem details as `application/problem+json` or `application/problem+xml`, delegating to the Jackson and XML modules. Resources returning `ProblemDetail` negotiate both media types through the `Accept` header:

```java
@GET
@Produces({"application/problem+json", "application/problem+xml"})
public ProblemDetail get() {
    return problem;
}
```

- `ProblemDetailExceptionMapper` maps `ProblemDetailException` to error responses: status from the detail (500 fallback, same rule as Spring), body pinned to `application/problem+json` so error responses stay deterministic while normal resources still negotiate.
