# XML

Artifact: `problem-details-xml`. Media type: `application/problem+xml`. No third-party dependencies — reading and writing use the JDK's streaming API.

```java
String xml = ProblemXml.toXml(problem);
ProblemDetail readBack = ProblemXml.fromXml(xml);
```

The format follows RFC 9457, Appendix B exactly: a `<problem xmlns="urn:ietf:rfc:7807">` root with one child element per member, and the appendix's object/array convention (an element whose children are all named `i` is an array).

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

Two asymmetries are inherent to the format and deliberate: all values travel as text, so extension scalars read back as `String` (no type guessing), and empty containers have no representation. Reading is otherwise lenient, mirroring the JSON module, and DTDs plus external entities are disabled.
