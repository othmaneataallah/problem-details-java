package io.github.othmaneataallah.problemdetails.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class SpringProblemDetailsTest {

  private static final ProblemDetail OUT_OF_CREDIT =
      ProblemDetail.builder()
          .type("https://example.com/probs/out-of-credit")
          .title("You do not have enough credit.")
          .status(403)
          .detail("Your current balance is 30, but that costs 50.")
          .instance("/account/12345/msgs/abc")
          .build();

  @Test
  void statusCodeMapsPresentStatus() {
    assertThat(SpringProblemDetails.statusCode(OUT_OF_CREDIT)).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void absentStatusMapsToInternalServerError() {
    ProblemDetail statusless = ProblemDetail.builder().title("Boom.").build();

    assertThat(SpringProblemDetails.statusCode(statusless))
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void convertsAllMembersAndExtensions() {
    org.springframework.http.ProblemDetail springDetail =
        SpringProblemDetails.toSpringDetail(
            ProblemDetail.builder()
                .type("https://example.com/probs/out-of-credit")
                .title("You do not have enough credit.")
                .status(403)
                .detail("Your current balance is 30, but that costs 50.")
                .instance("/account/12345/msgs/abc")
                .extension(
                    io.github.othmaneataallah.problemdetails.core.ProblemDetailKey.of(
                        "balance", Integer.class),
                    30)
                .build());

    assertThat(springDetail.getType().toString())
        .isEqualTo("https://example.com/probs/out-of-credit");
    assertThat(springDetail.getTitle()).isEqualTo("You do not have enough credit.");
    assertThat(springDetail.getStatus()).isEqualTo(403);
    assertThat(springDetail.getDetail())
        .isEqualTo("Your current balance is 30, but that costs 50.");
    assertThat(springDetail.getInstance().toString()).isEqualTo("/account/12345/msgs/abc");
    assertThat(springDetail.getProperties()).containsEntry("balance", 30);
  }

  @Test
  void responseEntityCarriesStatusAndProblemJsonContentType() {
    ResponseEntity<org.springframework.http.ProblemDetail> response =
        SpringProblemDetails.toResponseEntity(OUT_OF_CREDIT);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(response.getBody().getTitle()).isEqualTo("You do not have enough credit.");
  }

  @Test
  void convertsTitlelessProblem() {
    ProblemDetail titleless = ProblemDetail.builder().status(404).build();

    org.springframework.http.ProblemDetail springDetail =
        SpringProblemDetails.toSpringDetail(titleless);

    // Spring's forStatus pre-fills the title with the status reason phrase.
    assertThat(springDetail.getTitle()).isEqualTo("Not Found");
    assertThat(springDetail.getStatus()).isEqualTo(404);
  }

  @Test
  void nullProblemIsRejected() {
    assertThatThrownBy(() -> SpringProblemDetails.statusCode(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> SpringProblemDetails.toSpringDetail(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> SpringProblemDetails.toResponseEntity(null))
        .isInstanceOf(NullPointerException.class);
  }
}
