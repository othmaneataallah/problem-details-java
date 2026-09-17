package io.github.othmaneataallah.problemdetails.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ProblemTypeTest {

  @Test
  void factoryExposesAllMembers() {
    ProblemType type =
        ProblemType.of(
            URI.create("https://example.com/probs/out-of-credit"),
            "You do not have enough credit.",
            403,
            "https://example.com/docs/probs");

    assertThat(type.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
    assertThat(type.getTitle()).isEqualTo("You do not have enough credit.");
    assertThat(type.getRecommendedStatus()).isEqualTo(403);
    assertThat(type.getReference()).isEqualTo("https://example.com/docs/probs");
  }

  @Test
  void stringOverloadParsesTypeUri() {
    ProblemType type =
        ProblemType.of(
            "https://example.com/probs/out-of-credit",
            "You do not have enough credit.",
            403,
            "https://example.com/docs/probs");

    assertThat(type.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
  }

  @Test
  void statusAndReferenceMayBeAbsent() {
    ProblemType type =
        ProblemType.of(URI.create("about:blank"), "See HTTP Status Code", null, null);

    assertThat(type.getRecommendedStatus()).isNull();
    assertThat(type.getReference()).isNull();
  }

  @Test
  void nullTypeOrTitleIsRejected() {
    assertThatThrownBy(() -> ProblemType.of((URI) null, "Title", 400, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemType.of(URI.create("https://example.com/x"), null, 400, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemType.of((String) null, "Title", 400, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void blankTitleIsRejected() {
    assertThatThrownBy(() -> ProblemType.of(URI.create("https://example.com/x"), "  ", 400, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void invalidTypeUriIsRejected() {
    assertThatThrownBy(() -> ProblemType.of("not a valid uri", "Title", 400, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void statusBoundariesAreAccepted() {
    assertThat(
            ProblemType.of(URI.create("https://example.com/x"), "Title", 100, null)
                .getRecommendedStatus())
        .isEqualTo(100);
    assertThat(
            ProblemType.of(URI.create("https://example.com/x"), "Title", 599, null)
                .getRecommendedStatus())
        .isEqualTo(599);
  }

  @Test
  void statusOutsideHttpRangeIsRejected() {
    assertThatThrownBy(() -> ProblemType.of(URI.create("https://example.com/x"), "Title", 99, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> ProblemType.of(URI.create("https://example.com/x"), "Title", 600, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void valueSemantics() {
    ProblemType first =
        ProblemType.of(
            URI.create("https://example.com/probs/out-of-credit"),
            "You do not have enough credit.",
            403,
            "https://example.com/docs/probs");
    ProblemType second =
        ProblemType.of(
            "https://example.com/probs/out-of-credit",
            "You do not have enough credit.",
            403,
            "https://example.com/docs/probs");

    assertThat(first).isEqualTo(first);
    assertThat(first).isEqualTo(second);
    assertThat(second).isEqualTo(first);
    assertThat(first.hashCode()).isEqualTo(second.hashCode());
    assertThat(first).isNotEqualTo(null);
    assertThat(first).isNotEqualTo("not a problem type");
    assertThat(first.toString()).contains("out-of-credit", "403");
  }

  @Test
  void inequalityIsDetectedPerMember() {
    ProblemType base =
        ProblemType.of(
            URI.create("https://example.com/probs/a"), "Title.", 400, "https://example.com/docs");

    assertThat(base)
        .isNotEqualTo(
            ProblemType.of(
                URI.create("https://example.com/probs/b"),
                "Title.",
                400,
                "https://example.com/docs"));
    assertThat(base)
        .isNotEqualTo(
            ProblemType.of(
                URI.create("https://example.com/probs/a"),
                "Other.",
                400,
                "https://example.com/docs"));
    assertThat(base)
        .isNotEqualTo(
            ProblemType.of(
                URI.create("https://example.com/probs/a"),
                "Title.",
                404,
                "https://example.com/docs"));
    assertThat(base)
        .isNotEqualTo(
            ProblemType.of(
                URI.create("https://example.com/probs/a"),
                "Title.",
                400,
                "https://example.com/other-docs"));
  }

  @Test
  void toStringOmitsAbsentMembers() {
    ProblemType minimal =
        ProblemType.of(URI.create("about:blank"), "See HTTP Status Code", null, null);

    assertThat(minimal.toString())
        .isEqualTo("ProblemType[type=about:blank, title=See HTTP Status Code]");
  }
}
