package io.github.othmaneataallah.problemdetails.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
  void survivesJavaSerialization() throws Exception {
    ProblemDetailException original =
        new ProblemDetailException(
            ProblemDetail.builder()
                .title("Title.")
                .status(400)
                .extension(ProblemDetailKey.of("balance", Integer.class), 30)
                .build());

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(original);
    }
    ProblemDetailException readBack;
    try (ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      readBack = (ProblemDetailException) in.readObject();
    }

    assertThat(readBack.getProblemDetail()).isEqualTo(original.getProblemDetail());
    assertThat(readBack.getMessage()).isEqualTo(original.getMessage());
  }

  @Test
  void nullProblemDetailIsRejected() {

    assertThatThrownBy(() -> new ProblemDetailException(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new ProblemDetailException(null, new RuntimeException()))
        .isInstanceOf(NullPointerException.class);
  }
}
