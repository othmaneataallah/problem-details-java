# JSON

Artifact: `problem-details-jackson`. Media type: `application/problem+json`. Built on Jackson 3.

## Setup

Add the artifact next to `problem-details-core` (same group ID, same version), then register the module once on your mapper:

```java
JsonMapper mapper = JsonMapper.builder()
    .addModule(new ProblemDetailsModule())
    .build();
```

Without the module, Jackson sees an ordinary bean — register it; there is no auto-detection.

## Writing

Members serialize in RFC order — `type`, `status`, `title`, `detail`, `instance` — followed by extensions in the order you put them. Absent members are omitted; `type` is always present because the core defaults it to `about:blank`:

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

Extension values use Jackson's default value serialization, so strings, numbers, booleans, lists, and maps all work, nested as deeply as you like.

## Reading

```java
ProblemDetail problem = mapper.readValue(json, ProblemDetail.class);
```

Reading is lenient, exactly as RFC 9457, Section 3.1 requires: a member with the wrong type is *ignored*, as if it weren't there. A string where `status` belongs, an unparseable URI, or a JSON `null` all fall back to absent (and `type` then falls back to `about:blank`). Out-of-range or fractional statuses are ignored the same way.

Unknown members are never dropped — they become extensions, so parse/serialize round-trips are lossless and newer producers don't break older consumers. Extension values map to Java values as follows:

| JSON         | Java                                            |
|--------------|-------------------------------------------------|
| string       | `String`                                        |
| integer      | `Integer` (or `Long` past int range)            |
| decimal      | `Double`                                        |
| boolean      | `Boolean`                                       |
| array        | `ArrayList`                                     |
| object       | `LinkedHashMap`                                 |

Because extension lookup is name-based, you read values back through your own keys regardless of how they arrived:

```java
problem.get(ProblemDetailKey.of("balance", Integer.class)) // Optional[30]
```
