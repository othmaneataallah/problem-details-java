# JAX-RS

Artifact: `problem-details-jaxrs` (Jakarta REST 4; the API dependency is `provided` scope). Standard providers — register them on the application:

- `ProblemDetailMessageBodyWriter` serializes problem details as `application/problem+json` or `application/problem+xml` (content negotiation supported).
- `ProblemDetailExceptionMapper` maps `ProblemDetailException` to error responses: status from the detail (500 fallback), body pinned to `application/problem+json` for determinism.

```java
@GET
@Produces({"application/problem+json", "application/problem+xml"})
public ProblemDetail get() {
    return problem;
}
```
