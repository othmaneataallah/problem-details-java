package io.github.othmaneataallah.problemdetails.registry;

import java.net.URI;
import java.util.Objects;

/**
 * The immutable metadata of one problem type, following the registration template of RFC 9457,
 * Section 4.2: a type URI, a title, a recommended HTTP status code, and a reference to the defining
 * specification.
 *
 * <p>Instances are created with {@link #of(URI, String, Integer, String)} and are immutable and
 * thread-safe. The well-known {@code about:blank} entry is available as {@link
 * ProblemTypes#ABOUT_BLANK}, and custom types belong in a {@link ProblemTypeRegistry}.
 */
public final class ProblemType {

  private final URI type;
  private final String title;
  private final Integer recommendedStatus;
  private final String reference;

  private ProblemType(URI type, String title, Integer recommendedStatus, String reference) {
    this.type = type;
    this.title = title;
    this.recommendedStatus = recommendedStatus;
    this.reference = reference;
  }

  /**
   * Creates the metadata of one problem type.
   *
   * <p>The range check on the recommended status is a project decision, not an RFC requirement: RFC
   * 9457, Section 4.2 asks for "the HTTP status code for it to be used with", and this library pins
   * that to the 100–599 range of HTTP status codes, consistent with {@code ProblemDetail}.
   *
   * @param type the type URI identifying the problem type; must not be null
   * @param title the short title describing the problem type; must not be null or blank
   * @param recommendedStatus the recommended HTTP status code, or null when the type carries no
   *     recommendation (as for {@code about:blank})
   * @param reference the reference to the defining specification; may be null
   * @return a new problem type
   * @throws NullPointerException if {@code type} or {@code title} is null
   * @throws IllegalArgumentException if {@code title} is blank, or {@code recommendedStatus} is set
   *     outside the 100–599 range
   */
  public static ProblemType of(
      URI type, String title, Integer recommendedStatus, String reference) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(title, "title");
    if (title.isBlank()) {
      throw new IllegalArgumentException("Problem type title must not be blank");
    }
    if (recommendedStatus != null && (recommendedStatus < 100 || recommendedStatus > 599)) {
      throw new IllegalArgumentException(
          "Recommended status must be an HTTP status code between 100 and 599 but was "
              + recommendedStatus);
    }
    return new ProblemType(type, title, recommendedStatus, reference);
  }

  /**
   * Creates the metadata of one problem type from URI string forms.
   *
   * @param type the type URI, for example {@code "https://example.com/probs/out-of-credit"}; must
   *     not be null
   * @param title the short title describing the problem type; must not be null or blank
   * @param recommendedStatus the recommended HTTP status code, or null when the type carries no
   *     recommendation
   * @param reference the reference to the defining specification; may be null
   * @return a new problem type
   * @throws NullPointerException if {@code type} or {@code title} is null
   * @throws IllegalArgumentException if {@code type} is not a valid URI, {@code title} is blank, or
   *     {@code recommendedStatus} is set outside the 100–599 range
   */
  public static ProblemType of(
      String type, String title, Integer recommendedStatus, String reference) {
    return of(
        URI.create(Objects.requireNonNull(type, "type")), title, recommendedStatus, reference);
  }

  /**
   * Returns the type URI identifying the problem type.
   *
   * @return the type URI; never null
   */
  public URI getType() {
    return type;
  }

  /**
   * Returns the short title describing the problem type.
   *
   * @return the title; never null or blank
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the recommended HTTP status code for the problem type.
   *
   * @return the recommended status code, or null when the type carries no recommendation
   */
  public Integer getRecommendedStatus() {
    return recommendedStatus;
  }

  /**
   * Returns the reference to the specification defining the problem type.
   *
   * @return the reference, or null if none was given
   */
  public String getReference() {
    return reference;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProblemType other)) {
      return false;
    }
    return type.equals(other.type)
        && title.equals(other.title)
        && Objects.equals(recommendedStatus, other.recommendedStatus)
        && Objects.equals(reference, other.reference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, recommendedStatus, reference);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ProblemType[");
    result.append("type=").append(type).append(", title=").append(title);
    if (recommendedStatus != null) {
      result.append(", recommendedStatus=").append(recommendedStatus);
    }
    if (reference != null) {
      result.append(", reference=").append(reference);
    }
    return result.append(']').toString();
  }
}
