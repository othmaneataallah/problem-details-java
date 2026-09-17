package io.github.othmaneataallah.problemdetails.registry;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Well-known problem types and one-liner lookups.
 *
 * <p>The {@link #lookup(URI)} helpers consult a shared read-only registry holding the
 * spec-registered types. Applications with custom types should keep their own {@link
 * ProblemTypeRegistry} instead.
 */
public final class ProblemTypes {

  /**
   * The {@code about:blank} problem type registered by RFC 9457, Section 4.2.1: it indicates that
   * the problem has no additional semantics beyond that of the HTTP status code.
   *
   * <p>Note the entry's title, {@code "See HTTP Status Code"}, is not a usable title — per Section
   * 4.2.1 the title <em>should</em> be the recommended HTTP status phrase for the response's status
   * code (for example {@code "Not Found"} for 404), localized to suit client preferences.
   */
  public static final ProblemType ABOUT_BLANK =
      ProblemType.of(URI.create("about:blank"), "See HTTP Status Code", null, "RFC 9457");

  /**
   * The URI prefix that registrations in the "HTTP Problem Types" registry may use for their type
   * URI (RFC 9457, Section 4.2). Note that such URIs may not resolve.
   */
  public static final String IANA_PREFIX = "https://iana.org/assignments/http-problem-types#";

  private ProblemTypes() {}

  /**
   * Looks up a well-known problem type by URI.
   *
   * @param type the type URI to look up; must not be null
   * @return the problem type, or an empty {@code Optional} if the URI is not a well-known type
   * @throws NullPointerException if {@code type} is null
   */
  public static Optional<ProblemType> lookup(URI type) {
    return Defaults.REGISTRY.lookup(Objects.requireNonNull(type, "type"));
  }

  /**
   * Looks up a well-known problem type by URI string.
   *
   * @param type the type URI to look up; must not be null
   * @return the problem type, or an empty {@code Optional} if the URI is not a well-known type
   * @throws NullPointerException if {@code type} is null
   * @throws IllegalArgumentException if {@code type} is not a valid URI
   */
  public static Optional<ProblemType> lookup(String type) {
    return Defaults.REGISTRY.lookup(Objects.requireNonNull(type, "type"));
  }

  /**
   * Holder of the shared read-only registry. Initialization on demand guarantees the holder is
   * created long after {@link #ABOUT_BLANK} is assigned, avoiding a static initialization cycle
   * with {@link ProblemTypeRegistry}.
   */
  private static final class Defaults {
    static final ProblemTypeRegistry REGISTRY = new ProblemTypeRegistry();
  }
}
