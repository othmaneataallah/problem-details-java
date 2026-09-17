package io.github.othmaneataallah.problemdetails.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

  @Test
  void concurrentRegistrationAndLookup() throws Exception {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      List<Future<ProblemType>> registered = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        final int index = i;
        registered.add(
            pool.submit(
                () -> {
                  ProblemType type =
                      ProblemType.of(
                          "https://example.com/probs/worker-" + index, "Worker.", 400, null);
                  registry.register(type);
                  return registry.lookup(type.getType()).orElseThrow();
                }));
      }
      for (Future<ProblemType> future : registered) {
        assertThat(future.get(10, TimeUnit.SECONDS).getTitle()).isEqualTo("Worker.");
      }
      assertThat(registry.registeredTypes()).hasSize(threads + 1);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void concurrentDuplicateRegistrationLeavesOneWinner() throws Exception {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();
    ProblemType type = ProblemType.of("https://example.com/probs/raced", "Raced.", 400, null);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      AtomicInteger registered = new AtomicInteger();
      AtomicInteger rejected = new AtomicInteger();
      List<Future<?>> races = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        races.add(
            pool.submit(
                () -> {
                  try {
                    registry.register(type);
                    registered.incrementAndGet();
                  } catch (IllegalArgumentException expected) {
                    rejected.incrementAndGet();
                  }
                }));
      }
      for (Future<?> race : races) {
        race.get(10, TimeUnit.SECONDS);
      }
      assertThat(registered.get()).isEqualTo(1);
      assertThat(rejected.get()).isEqualTo(1);
      assertThat(registry.lookup(type.getType())).contains(type);
    } finally {
      pool.shutdownNow();
    }
  }
}
