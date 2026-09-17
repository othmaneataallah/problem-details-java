# JSON

Artifact: `problem-details-jackson`. Media type: `application/problem+json`. Built on Jackson 3 (`tools.jackson.core:jackson-databind`).

```java
JsonMapper mapper = JsonMapper.builder()
    .addModule(new ProblemDetailsModule())
    .build();

String json = mapper.writeValueAsString(problem);
ProblemDetail readBack = mapper.readValue(json, ProblemDetail.class);
```

Members serialize in RFC order (`type`, `status`, `title`, `detail`, `instance`); absent members are omitted and extensions follow in insertion order:

```json
{
  "type": "https://example.com/probs/out-of-credit",
  "title": "You do not have enough credit.",
  "detail": "Your current balance is 30, but that costs 50.",
  "instance": "/account/12345/msgs/abc",
  "balance": 30,
  "accounts": ["/account/12345", "/account/67890"]
}
```

Reading is lenient per RFC 9457, Section 3.1: mistyped members are ignored, and unknown members are preserved as extensions (typed read-back works through your own `ProblemDetailKey`s). Round-trips are lossless for JSON-compatible values.
