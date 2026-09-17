package io.github.othmaneataallah.problemdetails.jaxrs;

import static io.github.othmaneataallah.problemdetails.jackson.ProblemMediaTypes.PROBLEM_JSON;
import static io.github.othmaneataallah.problemdetails.xml.ProblemMediaTypes.PROBLEM_XML;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import io.github.othmaneataallah.problemdetails.jackson.ProblemDetailsModule;
import io.github.othmaneataallah.problemdetails.xml.ProblemXml;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ProblemDetailProvidersTest extends JerseyTest {

  private static final ProblemDetail OUT_OF_CREDIT =
      ProblemDetail.builder()
          .type("https://example.com/probs/out-of-credit")
          .title("You do not have enough credit.")
          .status(403)
          .detail("Your current balance is 30, but that costs 50.")
          .extension(ProblemDetailKey.of("balance", Integer.class), 30)
          .build();

  private static final JsonMapper JSON =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  @Path("credit")
  public static class CreditResource {

    @GET
    @Produces({PROBLEM_JSON, PROBLEM_XML})
    public ProblemDetail get() {
      return OUT_OF_CREDIT;
    }

    @GET
    @Path("stringy")
    @Produces({PROBLEM_JSON, PROBLEM_XML})
    public ProblemDetail getStringy() {
      return ProblemDetail.builder()
          .type("https://example.com/probs/out-of-credit")
          .title("You do not have enough credit.")
          .status(403)
          .extension(ProblemDetailKey.of("balance", String.class), "30")
          .build();
    }

    @GET
    @Path("fail")
    @Produces(PROBLEM_JSON)
    public String fail() {
      throw new ProblemDetailException(OUT_OF_CREDIT);
    }

    @GET
    @Path("boom")
    @Produces(PROBLEM_JSON)
    public String boom() {
      throw new ProblemDetailException(ProblemDetail.builder().title("Boom.").build());
    }
  }

  @Override
  protected Application configure() {
    return new ResourceConfig(
        CreditResource.class,
        ProblemDetailMessageBodyWriter.class,
        ProblemDetailExceptionMapper.class);
  }

  @Test
  void resourceSerializesProblemJson() throws Exception {
    Response response = target("credit").request(PROBLEM_JSON).get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getMediaType()).isEqualTo(MediaType.valueOf(PROBLEM_JSON));
    assertThat(JSON.readValue(response.readEntity(String.class), ProblemDetail.class))
        .isEqualTo(OUT_OF_CREDIT);
  }

  @Test
  void resourceSerializesProblemXml() throws Exception {
    // The shared fixture carries an Integer extension, which the XML format cannot
    // round-trip (scalars read back as strings), so this path uses string-only content.
    Response response = target("credit/stringy").request(PROBLEM_XML).get();

    ProblemDetail expected =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .status(403)
            .extension(ProblemDetailKey.of("balance", String.class), "30")
            .build();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getMediaType()).isEqualTo(MediaType.valueOf(PROBLEM_XML));
    assertThat(ProblemXml.fromXml(response.readEntity(String.class))).isEqualTo(expected);
  }

  @Test
  void exceptionMapsToProblemJsonError() throws Exception {
    Response response = target("credit/fail").request(PROBLEM_JSON).get();

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getMediaType()).isEqualTo(MediaType.valueOf(PROBLEM_JSON));
    assertThat(JSON.readValue(response.readEntity(String.class), ProblemDetail.class))
        .isEqualTo(OUT_OF_CREDIT);
  }

  @Test
  void statuslessExceptionMapsToInternalServerError() throws Exception {
    Response response = target("credit/boom").request(PROBLEM_JSON).get();

    // Only the HTTP status line carries the 500 fallback; the body stays faithful to the
    // status-less problem detail.
    assertThat(response.getStatus()).isEqualTo(500);
    assertThat(JSON.readValue(response.readEntity(String.class), ProblemDetail.class))
        .isEqualTo(ProblemDetail.builder().title("Boom.").build());
  }
}
