package io.github.othmaneataallah.problemdetails.jackson;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson module registering the problem details serializer and deserializer.
 *
 * <p>Register it on any Jackson 3 {@code JsonMapper} to read and write {@link ProblemDetail}:
 *
 * <pre>{@code
 * JsonMapper mapper =
 *     JsonMapper.builder().addModule(new ProblemDetailsModule()).build();
 * }</pre>
 */
public final class ProblemDetailsModule extends SimpleModule {

  /** Creates a module registering the problem details serializer and deserializer. */
  public ProblemDetailsModule() {
    super("ProblemDetailsModule");
    addSerializer(ProblemDetail.class, new ProblemDetailSerializer());
    addDeserializer(ProblemDetail.class, new ProblemDetailDeserializer());
  }
}
