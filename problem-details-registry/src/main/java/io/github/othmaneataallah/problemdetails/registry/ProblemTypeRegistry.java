package io.github.othmaneataallah.problemdetails.registry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe registry of problem types, modelling the "HTTP Problem Types" registry of RFC 9457,
 * Section 4.2.
 *
 * <p>Every registry is preloaded with the one spec-registered type, {@code about:blank} (Section
 * 4.2.1). Application-specific types — which per Section 4.1 must be minted and documented under
 * the API owner's control — are added with {@link #register(ProblemType)} and found again with
 * {@link #lookup(URI)}:
 *
 * <pre>{@code
 * ProblemTypeRegistry registry = new ProblemTypeRegistry();
 * registry.register(
 *     ProblemType.of("https://example.com/probs/out-of-credit",
 *         "You do not have enough credit.", 403, "https://example.com/docs/probs"));
 *
 * ProblemType type = registry.lookup("https://example.com/probs/out-of-credit").orElseThrow();
 * }</pre>
 *
 * <p>Registries are independent: registering a type on one never affects another. All methods are
 * safe for concurrent use.
 */
public final class ProblemTypeRegistry {

  private final ConcurrentMap<URI, ProblemType> types = new ConcurrentHashMap<>();

  /** Creates a registry preloaded with {@code about:blank}. */
  public ProblemTypeRegistry() {
    types.put(ProblemTypes.ABOUT_BLANK.getType(), ProblemTypes.ABOUT_BLANK);
  }

  /**
   * Registers a custom problem type.
   *
   * <p>Registering fails fast on duplicates: a type URI identifies one problem type, so an
   * accidental re-registration — which would shadow the earlier entry, possibly an IANA one — is
   * rejected rather than silently applied.
   *
   * @param problemType the problem type to register; must not be null
   * @throws NullPointerException if {@code problemType} is null
   * @throws IllegalArgumentException if the type URI is already registered
   */
  public void register(ProblemType problemType) {
    Objects.requireNonNull(problemType, "problemType");
    ProblemType existing = types.putIfAbsent(problemType.getType(), problemType);
    if (existing != null) {
      throw new IllegalArgumentException(
          "Problem type is already registered: " + problemType.getType());
    }
  }

  /**
   * Looks up the problem type registered under the given URI.
   *
   * @param type the type URI to look up; must not be null
   * @return the registered problem type, or an empty {@code Optional} if the URI is unknown
   * @throws NullPointerException if {@code type} is null
   */
  public Optional<ProblemType> lookup(URI type) {
    Objects.requireNonNull(type, "type");
    return Optional.ofNullable(types.get(type));
  }

  /**
   * Looks up the problem type registered under the given URI string.
   *
   * @param type the type URI to look up; must not be null
   * @return the registered problem type, or an empty {@code Optional} if the URI is unknown
   * @throws NullPointerException if {@code type} is null
   * @throws IllegalArgumentException if {@code type} is not a valid URI
   */
  public Optional<ProblemType> lookup(String type) {
    return lookup(URI.create(Objects.requireNonNull(type, "type")));
  }

  /**
   * Returns a snapshot of the registered problem types, ordered by type URI.
   *
   * @return an unmodifiable snapshot; never null
   */
  public List<ProblemType> registeredTypes() {
    List<ProblemType> snapshot = new ArrayList<>(types.values());
    snapshot.sort(Comparator.comparing(problemType -> problemType.getType().toString()));
    return Collections.unmodifiableList(snapshot);
  }
}
