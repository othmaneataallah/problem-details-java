package io.github.othmaneataallah.problemdetails.jackson;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import java.util.Map;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializes a {@link ProblemDetail} to its RFC 9457, Section 3 JSON representation.
 *
 * <p>Members are written in the specification's order — {@code type}, {@code status}, {@code
 * title}, {@code detail}, {@code instance} — followed by extension members in insertion order.
 * Absent (null) members are omitted, except {@code type}, which the core model always carries
 * (defaulting to {@code about:blank}).
 *
 * <p>Extension values are written with the mapper's default value serialization, so any
 * JSON-compatible value (strings, numbers, booleans, lists, maps) round-trips through {@link
 * ProblemDetailDeserializer}.
 */
public final class ProblemDetailSerializer extends ValueSerializer<ProblemDetail> {

  /** Creates a serializer. */
  public ProblemDetailSerializer() {}

  @Override
  public void serialize(ProblemDetail value, JsonGenerator gen, SerializationContext ctxt) {
    gen.writeStartObject();
    gen.writeStringProperty("type", value.getType().toString());
    if (value.getStatus() != null) {
      gen.writeNumberProperty("status", value.getStatus());
    }
    if (value.getTitle() != null) {
      gen.writeStringProperty("title", value.getTitle());
    }
    if (value.getDetail() != null) {
      gen.writeStringProperty("detail", value.getDetail());
    }
    if (value.getInstance() != null) {
      gen.writeStringProperty("instance", value.getInstance().toString());
    }
    for (Map.Entry<String, Object> extension : value.extensionMembers().entrySet()) {
      gen.writeName(extension.getKey());
      ctxt.writeValue(gen, extension.getValue());
    }
    gen.writeEndObject();
  }
}
