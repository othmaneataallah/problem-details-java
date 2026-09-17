package io.github.othmaneataallah.problemdetails.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ProblemTypeRegistryTest {

  private static final ProblemType OUT_OF_CREDIT =
      ProblemType.of(
          "https://example.com/probs/out-of-credit",
          "You do not have enough credit.",
          403,
          "https://example.com/docs/probs");

  @Test
  void preloadedWithAboutBlank() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();

    assertThat(registry.lookup(URI.create("about:blank"))).contains(ProblemTypes.ABOUT_BLANK);
    assertThat(registry.registeredTypes()).containsExactly(ProblemTypes.ABOUT_BLANK);
  }

  @Test
  void customTypeRegistersAndLooksUp() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();
    registry.register(OUT_OF_CREDIT);

    assertThat(registry.lookup(URI.create("https://example.com/probs/out-of-credit")))
        .contains(OUT_OF_CREDIT);
    assertThat(registry.lookup("https://example.com/probs/out-of-credit")).contains(OUT_OF_CREDIT);
    assertThat(registry.lookup("https://example.com/probs/unknown")).isEmpty();
  }

  @Test
  void duplicateRegistrationIsRejected() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();
    registry.register(OUT_OF_CREDIT);

    assertThatThrownBy(() -> registry.register(OUT_OF_CREDIT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("https://example.com/probs/out-of-credit");
    assertThatThrownBy(() -> registry.register(ProblemTypes.ABOUT_BLANK))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("about:blank");
  }

  @Test
  void nullRegistrationOrLookupIsRejected() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();

    assertThatThrownBy(() -> registry.register(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> registry.lookup((URI) null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> registry.lookup((String) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void invalidLookupUriIsRejected() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();

    assertThatThrownBy(() -> registry.lookup("not a valid uri"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void registriesAreIndependent() {
    ProblemTypeRegistry first = new ProblemTypeRegistry();
    ProblemTypeRegistry second = new ProblemTypeRegistry();
    first.register(OUT_OF_CREDIT);

    assertThat(second.lookup(OUT_OF_CREDIT.getType())).isEmpty();
    assertThat(first.registeredTypes()).hasSize(2);
    assertThat(second.registeredTypes()).hasSize(1);
  }

  @Test
  void snapshotIsUnmodifiableAndOrdered() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();
    ProblemType other =
        ProblemType.of("https://example.com/probs/aardvark", "Aardvark.", 400, null);
    registry.register(OUT_OF_CREDIT);
    registry.register(other);

    assertThat(registry.registeredTypes())
        .containsExactly(ProblemTypes.ABOUT_BLANK, other, OUT_OF_CREDIT);
    assertThatThrownBy(() -> registry.registeredTypes().add(other))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
