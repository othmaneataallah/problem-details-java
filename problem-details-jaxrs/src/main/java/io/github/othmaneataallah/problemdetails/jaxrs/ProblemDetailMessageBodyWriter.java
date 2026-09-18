package io.github.othmaneataallah.problemdetails.jaxrs;

import static io.github.othmaneataallah.problemdetails.jackson.ProblemMediaTypes.PROBLEM_JSON;
import static io.github.othmaneataallah.problemdetails.xml.ProblemMediaTypes.PROBLEM_XML;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.jackson.ProblemDetailsModule;
import io.github.othmaneataallah.problemdetails.xml.ProblemXml;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Serializes {@link ProblemDetail} as {@code application/problem+json} or {@code
 * application/problem+xml}, delegating to the Jackson and XML modules.
 */
@Provider
@Produces({PROBLEM_JSON, PROBLEM_XML})
public final class ProblemDetailMessageBodyWriter implements MessageBodyWriter<ProblemDetail> {

  private static final MediaType PROBLEM_JSON_TYPE = MediaType.valueOf(PROBLEM_JSON);
  private static final MediaType PROBLEM_XML_TYPE = MediaType.valueOf(PROBLEM_XML);

  private static final JsonMapper JSON =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  /** Creates a writer. */
  public ProblemDetailMessageBodyWriter() {
    // Stateless; instantiated by the JAX-RS runtime.
  }

  @Override
  public boolean isWriteable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return ProblemDetail.class.isAssignableFrom(type)
        && (PROBLEM_JSON_TYPE.isCompatible(mediaType) || PROBLEM_XML_TYPE.isCompatible(mediaType));
  }

  @Override
  public void writeTo(
      ProblemDetail problem,
      Class<?> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException {
    String body;
    if (PROBLEM_XML_TYPE.isCompatible(mediaType) && !PROBLEM_JSON_TYPE.isCompatible(mediaType)) {
      try {
        body = ProblemXml.toXml(problem);
      } catch (XMLStreamException e) {
        throw new WebApplicationException(
            "Failed to serialize problem detail as XML",
            e,
            jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      body = JSON.writeValueAsString(problem);
    }
    entityStream.write(body.getBytes(StandardCharsets.UTF_8));
  }
}
