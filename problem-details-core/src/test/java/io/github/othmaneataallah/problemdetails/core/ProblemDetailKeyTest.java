package io.github.othmaneataallah.problemdetails.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProblemDetailKeyTest {

  @Test
  void keyExposesNameAndType() {
    ProblemDetailKey<Integer> key = ProblemDetailKey.of("balance", Integer.class);

    assertThat(key.name()).isEqualTo("balance");
    assertThat(key.type()).isEqualTo(Integer.class);
  }

  @Test
  void nullNameOrTypeIsRejected() {
    assertThatThrownBy(() -> ProblemDetailKey.of(null, String.class))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemDetailKey.of("balance", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void blankNameIsRejected() {
    assertThatThrownBy(() -> ProblemDetailKey.of("", String.class))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ProblemDetailKey.of("   ", String.class))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void equalityIsByNameOnly() {
    assertThat(ProblemDetailKey.of("balance", Integer.class))
        .isEqualTo(ProblemDetailKey.of("balance", Integer.class));
    assertThat(ProblemDetailKey.of("balance", Integer.class))
        .isEqualTo(ProblemDetailKey.of("balance", String.class));
    assertThat(ProblemDetailKey.of("balance", String.class))
        .isEqualTo(ProblemDetailKey.of("balance", Integer.class));
    assertThat(ProblemDetailKey.of("balance", Integer.class))
        .isNotEqualTo(ProblemDetailKey.of("other", Integer.class));
    assertThat(ProblemDetailKey.of("balance", Integer.class)).isNotEqualTo("balance");
    assertThat(ProblemDetailKey.of("balance", Integer.class)).isNotEqualTo(null);
    assertThat(ProblemDetailKey.of("balance", Integer.class).hashCode())
        .isEqualTo(ProblemDetailKey.of("balance", String.class).hashCode());
  }

  @Test
  void selfEquality() {
    ProblemDetailKey<Integer> key = ProblemDetailKey.of("balance", Integer.class);

    assertThat(key).isEqualTo(key);
  }

  @Test
  void castChecksValueType() {
    ProblemDetailKey<Integer> key = ProblemDetailKey.of("balance", Integer.class);

    assertThat(key.cast(30)).isEqualTo(30);
    assertThat(key.cast(null)).isNull();
    assertThatThrownBy(() -> key.cast("not an integer")).isInstanceOf(ClassCastException.class);
  }

  @Test
  void toStringMentionsNameAndType() {
    assertThat(ProblemDetailKey.of("balance", Integer.class).toString())
        .contains("balance", Integer.class.getName());
  }
}
