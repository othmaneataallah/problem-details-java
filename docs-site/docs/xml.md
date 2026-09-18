# XML

Artifact: `problem-details-xml`. Media type: `application/problem+xml`. Nothing extra to install — reading and writing use the XML support already in the JDK.

```java
String xml = ProblemXml.toXml(problem);
ProblemDetail readBack = ProblemXml.fromXml(xml);
```

(Streaming variants taking `XMLStreamWriter`/`XMLStreamReader` exist for pipelines. Everything reports failures as the checked `XMLStreamException` — one exception type for all of it.)

## What the XML looks like

One child element per field, extensions after the standard ones:

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

Two things to know, both inherited from the format itself rather than this library: an element with children is an *object*, except one whose children are all named `i` — that's an *array*. And yes, the namespace really says `7807`: the RFC kept it from the older spec it replaced.

Only `String`, numbers, booleans, lists, and maps can be written — anything else fails fast with a clear error instead of silent garbage. The same goes for field names that aren't valid XML.

## Two quirks worth knowing upfront

1. **Numbers come back as strings.** XML carries no types, so `<balance>30</balance>` reads back as `"30"`. Nothing is guessed — which is also what keeps values like `"01234"` intact. If typed round-trips matter to you, use JSON.
2. **Empty lists and maps have no representation.** They write as an empty element and read back as an empty string.

Otherwise reading is as forgiving as the JSON side: bad URIs, bad statuses, and markup where text belongs are skipped; unknown elements become extensions; stray whitespace is trimmed. And hostile input (entity bombs and the like) is rejected — DTDs and external entities stay off.
