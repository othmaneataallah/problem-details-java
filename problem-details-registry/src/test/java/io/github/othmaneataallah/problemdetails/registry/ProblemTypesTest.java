package io.github.othmaneataallah.problemdetails.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ProblemTypesTest {

  @Test
  void aboutBlankMatchesRfcRegistration() {
    assertThat(ProblemTypes.ABOUT_BLANK.getType()).isEqualTo(URI.create("about:blank"));
    assertThat(ProblemTypes.ABOUT_BLANK.getTitle()).isEqualTo("See HTTP Status Code");
    assertThat(ProblemTypes.ABOUT_BLANK.getRecommendedStatus()).isNull();
    assertThat(ProblemTypes.ABOUT_BLANK.getReference()).isEqualTo("RFC 9457");
  }

  @Test
  void ianaPrefix() {
    assertThat(ProblemTypes.IANA_PREFIX)
        .isEqualTo("https://iana.org/assignments/http-problem-types#");
  }

  @Test
  void lookupFindsWellKnownTypes() {
    assertThat(ProblemTypes.lookup(URI.create("about:blank"))).contains(ProblemTypes.ABOUT_BLANK);
    assertThat(ProblemTypes.lookup("about:blank")).contains(ProblemTypes.ABOUT_BLANK);
    assertThat(ProblemTypes.lookup("https://example.com/probs/unknown")).isEmpty();
  }

  @Test
  void nullLookupIsRejected() {
    assertThatThrownBy(() -> ProblemTypes.lookup((URI) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemTypes.lookup((String) null))
        .isInstanceOf(NullPointerException.class);
  }
}
