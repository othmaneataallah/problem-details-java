package io.github.othmaneataallah.problemdetails.spring;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import io.github.othmaneataallah.problemdetails.jackson.ProblemDetailsModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

class WebFluxAdviceTest {

  private static final ProblemDetail OUT_OF_CREDIT =
      ProblemDetail.builder()
          .type("https://example.com/probs/out-of-credit")
          .title("You do not have enough credit.")
          .status(403)
          .detail("Your current balance is 30, but that costs 50.")
          .instance("https://example.com/instances/1")
          .extension(ProblemDetailKey.of("balance", Integer.class), 30)
          .build();

  private static final JsonMapper MAPPER =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  @RestController
  static class CreditController {

    @GetMapping("/credit")
    public String credit() {
      throw new ProblemDetailException(OUT_OF_CREDIT);
    }

    @GetMapping("/boom")
    public String boom() {
      throw new ProblemDetailException(ProblemDetail.builder().title("Boom.").build());
    }
  }

  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient =
        WebTestClient.bindToController(new CreditController())
            .controllerAdvice(new ProblemDetailAdvice())
            .build();
  }

  @Test
  void exceptionRendersProblemJsonError() {
    webTestClient
        .get()
        .uri("/credit")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectHeader()
        .contentType("application/problem+json")
        .expectBody(String.class)
        .value(
            body ->
                assertThat(MAPPER.readValue(body, ProblemDetail.class)).isEqualTo(OUT_OF_CREDIT));
  }

  @Test
  void statuslessProblemRendersInternalServerError() {
    webTestClient
        .get()
        .uri("/boom")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectHeader()
        .contentType("application/problem+json")
        .expectBody(String.class)
        .value(
            body ->
                assertThat(MAPPER.readValue(body, ProblemDetail.class))
                    .isEqualTo(
                        ProblemDetail.builder()
                            .title("Boom.")
                            .status(500)
                            .instance("/boom")
                            .build()));
  }
}
