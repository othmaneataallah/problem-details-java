# JSON

Artifact: `problem-details-jackson`. Media type: `application/problem+json`. Needs Jackson 3.

## Setup

Add the artifact next to `problem-details-core` (same group ID, same version), then register the module once:

```java
JsonMapper mapper = JsonMapper.builder()
    .addModule(new ProblemDetailsModule())
    .build();
```

That one registration is what teaches Jackson the format — without it you get an ordinary bean dump.

## Writing

Fields come out in the RFC's order, missing fields are simply left out, and your custom fields follow in the order you added them. (`type` is always there — the core fills in `about:blank` for you.)

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

Custom values can be strings, numbers, booleans, lists, or maps, nested as deeply as you like.

## Reading

```java
ProblemDetail problem = mapper.readValue(json, ProblemDetail.class);
```

Reading forgives a lot, by design: a field with the wrong type is skipped as if it weren't there, JSON `null`s count as absent, and an out-of-range status is dropped. Unknown fields are never lost — they become extensions, so an older reader still round-trips a newer producer's output without damage.

Values land in natural Java types (strings stay strings, whole numbers become `Integer` or `Long`, decimals become `Double`, arrays become lists, objects become maps), and you read them back through your own keys:

```java
problem.get(ProblemDetailKey.of("balance", Integer.class)) // Optional[30]
```
