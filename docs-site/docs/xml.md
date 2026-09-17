# XML

Artifact: `problem-details-xml`. Media type: `application/problem+xml`. No third-party dependencies — reading and writing use the XML streaming API built into the JDK.

```java
String xml = ProblemXml.toXml(problem);
ProblemDetail readBack = ProblemXml.fromXml(xml);
```

`toXml`/`fromXml` overloads also accept raw `XMLStreamWriter`/`XMLStreamReader` for streaming pipelines. Everything throws the checked `XMLStreamException` on failure — one exception type for I/O problems, malformed input, and rejected content.

## The format

The output follows RFC 9457, Appendix B exactly: a `<problem>` root in the `urn:ietf:rfc:7807` namespace (kept from the obsoleted RFC 7807 — that odd-looking `7807` is correct), one child element per member in schema order, extensions after the standard members:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<problem xmlns="urn:ietf:rfc:7807">
  <type>https://example.com/probs/out-of-credit</type>
  <title>You do not have enough credit.</title>
  <detail>Your current balance is 30, but that costs 50.</detail>
  <instance>https://example.net/account/12345/msgs/abc</instance>
  <balance>30</balance>
  <accounts>
    <i>https://example.net/account/12345</i>
    <i>https://example.net/account/67890</i>
  </accounts>
</problem>
```

Extensions follow the appendix's convention: an element with children is an **object**, except an element whose children are all named **`i`**, which is an **array**. Supported value types are `String`, `Number`, `Boolean`, `Character`, `List`, and `Map` — anything else fails fast with a clear error, as do extension names that aren't valid XML element names.

## Two honest asymmetries

These come from the format itself, not from this library:

1. **Everything travels as text.** `<balance>30</balance>` reads back as the *string* `"30"`. No type guessing is applied — which also keeps values like `"01234"` intact.
2. **Empty containers have no representation.** An empty list or map writes as an empty element and reads back as an empty string.

## Reading rules

Mirroring the JSON module: unparseable URIs, non-numeric or out-of-range statuses, and markup where a scalar belongs are ignored; unknown elements become extensions; scalar text is trimmed (insignificant whitespace is unavoidable in XML); the root must be a `problem` element. DTDs and external entities are disabled, so XML bomb input is rejected outright.
