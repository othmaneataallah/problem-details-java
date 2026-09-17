package io.github.othmaneataallah.problemdetails.jackson;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserializes a {@link ProblemDetail} from its RFC 9457, Section 3 JSON representation.
 *
 * <p>Reading is lenient, following RFC 9457, Section 3.1: a member whose value does not match the
 * specified type <em>must be ignored</em>, with processing continuing as if the member had not been
 * present. Concretely:
 *
 * <ul>
 *   <li>a non-textual {@code title} or {@code detail} is ignored,
 *   <li>a non-integral or out-of-range {@code status} is ignored,
 *   <li>a non-textual or unparseable {@code type} or {@code instance} URI is ignored (an ignored
 *       {@code type} falls back to the core default, {@code about:blank}),
 *   <li>a JSON null member is treated as absent, mirroring how the serializer omits nulls.
 * </ul>
 *
 * <p>Every other member is an extension member (RFC 9457, Section 3.2) and is preserved — the RFC
 * requires consumers to tolerate unrecognized extensions, and keeping them makes parse/serialize
 * round-trips lossless. Extension values map to Java values as follows: JSON strings to {@code
 * String}, integers to {@code Integer} or {@code Long}, decimals to {@code Double}, booleans to
 * {@code Boolean}, arrays to {@code ArrayList}, objects to {@code LinkedHashMap}. Because extension
 * lookup is name-based, callers read values back through their own {@link ProblemDetailKey}s, for
 * example {@code problem.get(ProblemDetailKey.of("balance", Integer.class))}.
 */
public final class ProblemDetailDeserializer extends ValueDeserializer<ProblemDetail> {

  /** Creates a deserializer. */
  public ProblemDetailDeserializer() {}

  @Override
  public ProblemDetail deserialize(JsonParser p, DeserializationContext ctxt) {
    JsonNode node = ctxt.readTree(p);
    if (!node.isObject()) {
      return (ProblemDetail) ctxt.handleUnexpectedToken(ProblemDetail.class, p);
    }
    ProblemDetail.Builder builder = ProblemDetail.builder();

    JsonNode type = node.get("type");
    if (type != null && type.isTextual()) {
      try {
        builder.type(URI.create(type.asText()));
      } catch (IllegalArgumentException ignored) {
        // Fall through to the core default, per the ignore-mistyped-member rule above.
      }
    }
    JsonNode status = node.get("status");
    if (status != null && status.isIntegralNumber() && status.canConvertToInt()) {
      int value = status.intValue();
      if (value >= 100 && value <= 599) {
        builder.status(value);
      }
    }
    JsonNode title = node.get("title");
    if (title != null && title.isTextual()) {
      builder.title(title.asText());
    }
    JsonNode detail = node.get("detail");
    if (detail != null && detail.isTextual()) {
      builder.detail(detail.asText());
    }
    JsonNode instance = node.get("instance");
    if (instance != null && instance.isTextual()) {
      try {
        builder.instance(URI.create(instance.asText()));
      } catch (IllegalArgumentException ignored) {
        // Fall through to absent, per the ignore-mistyped-member rule above.
      }
    }

    for (Map.Entry<String, JsonNode> field : node.properties()) {
      String name = field.getKey();
      if (isStandardMember(name)) {
        continue;
      }
      JsonNode member = field.getValue();
      if (member.isNull()) {
        continue;
      }
      putExtension(builder, name, toValue(member));
    }
    return builder.build();
  }

  private static boolean isStandardMember(String name) {
    return name.equals("type")
        || name.equals("status")
        || name.equals("title")
        || name.equals("detail")
        || name.equals("instance");
  }

  @SuppressWarnings("unchecked")
  private static void putExtension(ProblemDetail.Builder builder, String name, Object value) {
    // Safe: the key only records the value's runtime type while lookups match by member name,
    // so the cast can never cause a heap-pollution failure for the stored value itself.
    Class<Object> type = (Class<Object>) value.getClass();
    builder.extension(ProblemDetailKey.of(name, type), value);
  }

  private static Object toValue(JsonNode node) {
    if (node.isTextual()) {
      return node.asText();
    }
    if (node.isIntegralNumber() || node.isFloatingPointNumber()) {
      return node.numberValue();
    }
    if (node.isBoolean()) {
      return node.booleanValue();
    }
    if (node.isArray()) {
      List<Object> values = new ArrayList<>();
      for (JsonNode element : node) {
        values.add(toValue(element));
      }
      return values;
    }
    Map<String, Object> values = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> field : node.properties()) {
      values.put(field.getKey(), toValue(field.getValue()));
    }
    return values;
  }
}
