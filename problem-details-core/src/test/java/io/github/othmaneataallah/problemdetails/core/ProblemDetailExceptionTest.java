package io.github.othmaneataallah.problemdetails.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ProblemDetailExceptionTest {

  @Test
  void carriesProblemDetail() {
    ProblemDetail problem = ProblemDetail.builder().title("Title").status(400).build();

    ProblemDetailException exception = new ProblemDetailException(problem);

    assertThat(exception.getProblemDetail()).isSameAs(problem);
  }

  @Test
  void carriesCause() {
    ProblemDetail problem = ProblemDetail.builder().build();
    RuntimeException cause = new RuntimeException("root");

    ProblemDetailException exception = new ProblemDetailException(problem, cause);

    assertThat(exception.getProblemDetail()).isSameAs(problem);
    assertThat(exception.getCause()).isSameAs(cause);
  }

  @Test
  void messagePrefersDetailThenTitleThenType() {
    assertThat(
            new ProblemDetailException(
                    ProblemDetail.builder().title("Title").detail("Detail").build())
                .getMessage())
        .isEqualTo("Detail");
    assertThat(
            new ProblemDetailException(ProblemDetail.builder().title("Title").build()).getMessage())
        .isEqualTo("Title");
    assertThat(
            new ProblemDetailException(
                    ProblemDetail.builder().type("https://example.com/probs/x").build())
                .getMessage())
        .isEqualTo("https://example.com/probs/x");
    assertThat(new ProblemDetailException(ProblemDetail.builder().build()).getMessage())
        .isEqualTo(URI.create("about:blank").toString());
  }

  @Test
  void nullProblemDetailIsRejected() {
    assertThatThrownBy(() -> new ProblemDetailException(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new ProblemDetailException(null, new RuntimeException()))
        .isInstanceOf(NullPointerException.class);
  }
}
