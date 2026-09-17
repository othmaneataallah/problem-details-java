package io.github.othmaneataallah.problemdetails.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ProblemDetailMessageBodyWriterTest {

  private static final MediaType PROBLEM_JSON = MediaType.valueOf("application/problem+json");
  private static final MediaType PROBLEM_XML = MediaType.valueOf("application/problem+xml");

  private final ProblemDetailMessageBodyWriter writer = new ProblemDetailMessageBodyWriter();

  @Test
  void writeableForProblemDetailsInBothMediaTypes() {
    assertThat(writer.isWriteable(ProblemDetail.class, null, null, PROBLEM_JSON)).isTrue();
    assertThat(writer.isWriteable(ProblemDetail.class, null, null, PROBLEM_XML)).isTrue();
    assertThat(writer.isWriteable(ProblemDetail.class, null, null, MediaType.WILDCARD_TYPE))
        .isTrue();
  }

  @Test
  void notWriteableForOtherTypesOrMediaTypes() {
    assertThat(writer.isWriteable(String.class, null, null, PROBLEM_JSON)).isFalse();
    assertThat(writer.isWriteable(ProblemDetail.class, null, null, MediaType.TEXT_PLAIN_TYPE))
        .isFalse();
  }

  @Test
  void writesJsonBytes() throws Exception {
    ProblemDetail problem = ProblemDetail.builder().title("Title.").status(400).build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    writer.writeTo(problem, null, null, null, PROBLEM_JSON, null, out);

    assertThat(out.toString(StandardCharsets.UTF_8))
        .isEqualTo("{\"type\":\"about:blank\",\"status\":400,\"title\":\"Title.\"}");
  }

  @Test
  void writesXmlBytes() throws Exception {
    ProblemDetail problem = ProblemDetail.builder().title("Title.").build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    writer.writeTo(problem, null, null, null, PROBLEM_XML, null, out);

    assertThat(out.toString(StandardCharsets.UTF_8))
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<problem xmlns=\"urn:ietf:rfc:7807\">"
                + "<type>about:blank</type><title>Title.</title>"
                + "</problem>");
  }

  @Test
  void defaultsToJsonForAmbiguousMediaType() throws Exception {
    ProblemDetail problem = ProblemDetail.builder().title("Title.").build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    writer.writeTo(problem, null, null, null, MediaType.WILDCARD_TYPE, null, out);

    assertThat(out.toString(StandardCharsets.UTF_8)).startsWith("{\"type\":\"about:blank\"");
  }

  @Test
  void xmlSerializationFailureBecomesServerError() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("not a name", String.class), "value")
            .build();

    assertThatThrownBy(
            () ->
                writer.writeTo(
                    problem, null, null, null, PROBLEM_XML, null, new ByteArrayOutputStream()))
        .isInstanceOf(WebApplicationException.class)
        .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
        .isEqualTo(500);
  }
}
