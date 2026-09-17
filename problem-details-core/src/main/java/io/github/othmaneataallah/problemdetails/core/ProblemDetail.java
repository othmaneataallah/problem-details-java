package io.github.othmaneataallah.problemdetails.core;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable RFC 9457 problem details object.
 *
 * <p>A problem detail carries machine-readable details of an error in addition to the HTTP status
 * code, so that HTTP APIs do not need to invent new error response formats (RFC 9457, Section 1).
 * The standard members are:
 *
 * <ul>
 *   <li>{@code type} (Section 3.1.1) — a URI reference identifying the problem type; when absent,
 *       consumers assume {@code "about:blank"},
 *   <li>{@code status} (Section 3.1.2) — the HTTP status code for this occurrence, advisory only,
 *   <li>{@code title} (Section 3.1.3) — a short, human-readable summary of the problem type,
 *   <li>{@code detail} (Section 3.1.4) — a human-readable explanation specific to this occurrence,
 *       not to be parsed by consumers,
 *   <li>{@code instance} (Section 3.1.5) — a URI reference identifying this specific occurrence.
 * </ul>
 *
 * <p>Problem-type-specific extension members (Section 3.2) are held under typed {@link
 * ProblemDetailKey}s and read back with static type safety via {@link #get(ProblemDetailKey)}. The
 * untyped {@link #extensionMembers()} view exists only for serialization modules; it is not the
 * primary API.
 *
 * <p>Instances are created with {@link #builder()} and are immutable and thread-safe. A builder may
 * be reused to create several instances; values are copied at {@link Builder#build() build} time,
 * so later builder mutations never affect already-built instances.
 *
 * <pre>{@code
 * ProblemDetail problem =
 *     ProblemDetail.builder()
 *         .type("https://example.com/probs/out-of-credit")
 *         .title("You do not have enough credit.")
 *         .status(403)
 *         .detail("Your current balance is 30, but that costs 50.")
 *         .instance("/account/12345/msgs/abc")
 *         .build();
 * }</pre>
 */
public final class ProblemDetail {

  /** The problem type assumed when {@code type} is absent (RFC 9457, Section 3.1.1). */
  public static final URI ABOUT_BLANK = URI.create("about:blank");

  private final URI type;
  private final String title;
  private final Integer status;
  private final String detail;
  private final URI instance;
  private final Map<ProblemDetailKey<?>, Object> extensions;
  private final Map<String, Object> extensionMembers;

  private ProblemDetail(Builder builder) {
    this.type = builder.type;
    this.title = builder.title;
    this.status = builder.status;
    this.detail = builder.detail;
    this.instance = builder.instance;
    this.extensions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.extensions));
    Map<String, Object> members = new LinkedHashMap<>();
    this.extensions.forEach((key, value) -> members.put(key.name(), value));
    this.extensionMembers = Collections.unmodifiableMap(members);
  }

  /**
   * Returns a new builder for a problem detail.
   *
   * @return a new, empty builder defaulting {@code type} to {@link #ABOUT_BLANK}
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the problem type identifier.
   *
   * <p>This is the {@code type} member of RFC 9457, Section 3.1.1: a URI reference that is the
   * primary identifier of the problem type. It is never null; a builder that never sets a type
   * produces {@link #ABOUT_BLANK}, matching the RFC's assumed value for an absent member.
   *
   * @return the problem type; never null
   */
  public URI getType() {
    return type;
  }

  /**
   * Returns the short, human-readable summary of the problem type.
   *
   * <p>This is the {@code title} member of RFC 9457, Section 3.1.3. It is advisory and should not
   * change from occurrence to occurrence except for localization.
   *
   * @return the title, or null if absent
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the HTTP status code for this occurrence of the problem.
   *
   * <p>This is the {@code status} member of RFC 9457, Section 3.1.2. It is advisory only; the
   * actual HTTP response carries the authoritative status code.
   *
   * @return the status code, or null if absent (for example outside an HTTP context)
   */
  public Integer getStatus() {
    return status;
  }

  /**
   * Returns the human-readable explanation specific to this occurrence.
   *
   * <p>This is the {@code detail} member of RFC 9457, Section 3.1.4. Consumers should not parse it;
   * machine-readable data belongs in extension members.
   *
   * @return the detail, or null if absent
   */
  public String getDetail() {
    return detail;
  }

  /**
   * Returns the URI reference identifying this specific occurrence.
   *
   * <p>This is the {@code instance} member of RFC 9457, Section 3.1.5. When dereferenceable it may
   * lead back to the problem details object; otherwise it is an opaque identifier.
   *
   * @return the instance identifier, or null if absent
   */
  public URI getInstance() {
    return instance;
  }

  /**
   * Returns the value of the extension member identified by the given key.
   *
   * @param key the extension key to look up; must not be null
   * @param <T> the value type bound to the key
   * @return the value, or an empty {@code Optional} if no value was put under the key's name
   * @throws NullPointerException if {@code key} is null
   */
  public <T> Optional<T> get(ProblemDetailKey<T> key) {
    Objects.requireNonNull(key, "key");
    Object value = extensions.get(key);
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(key.cast(value));
  }

  /**
   * Tells whether a value was put under the given key's name.
   *
   * @param key the extension key to look up; must not be null
   * @return true if an extension member with the key's name is present
   * @throws NullPointerException if {@code key} is null
   */
  public boolean has(ProblemDetailKey<?> key) {
    return extensions.containsKey(Objects.requireNonNull(key, "key"));
  }

  /**
   * Returns all extension members as an unmodifiable name-to-value view in insertion order.
   *
   * <p>This view exists for serialization modules (JSON, XML), which must write members the typed
   * API does not know statically. Application code should prefer {@link #get(ProblemDetailKey)}.
   *
   * @return an unmodifiable snapshot of the extension members; never null
   */
  public Map<String, Object> extensionMembers() {
    return extensionMembers;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProblemDetail other)) {
      return false;
    }
    return type.equals(other.type)
        && Objects.equals(title, other.title)
        && Objects.equals(status, other.status)
        && Objects.equals(detail, other.detail)
        && Objects.equals(instance, other.instance)
        && extensions.equals(other.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, status, detail, instance, extensions);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ProblemDetail[");
    result.append("type=").append(type);
    if (title != null) {
      result.append(", title=").append(title);
    }
    if (status != null) {
      result.append(", status=").append(status);
    }
    if (detail != null) {
      result.append(", detail=").append(detail);
    }
    if (instance != null) {
      result.append(", instance=").append(instance);
    }
    extensions.forEach(
        (key, value) -> result.append(", ").append(key.name()).append('=').append(value));
    return result.append(']').toString();
  }

  /**
   * Builder for {@link ProblemDetail}.
   *
   * <p>A builder instance is mutable and not thread-safe, but every {@link #build()} call produces
   * an independent immutable snapshot.
   */
  public static final class Builder {

    private URI type = ABOUT_BLANK;
    private String title;
    private Integer status;
    private String detail;
    private URI instance;
    private final Map<ProblemDetailKey<?>, Object> extensions = new LinkedHashMap<>();

    private Builder() {}

    /**
     * Sets the problem type identifier.
     *
     * @param type the type URI reference; must not be null
     * @return this builder
     * @throws NullPointerException if {@code type} is null
     */
    public Builder type(URI type) {
      this.type = Objects.requireNonNull(type, "type");
      return this;
    }

    /**
     * Sets the problem type identifier from its string form.
     *
     * @param type the type URI reference, for example {@code
     *     "https://example.com/probs/out-of-credit"}; must not be null
     * @return this builder
     * @throws NullPointerException if {@code type} is null
     * @throws IllegalArgumentException if {@code type} is not a valid URI reference
     */
    public Builder type(String type) {
      return type(URI.create(Objects.requireNonNull(type, "type")));
    }

    /**
     * Sets the short, human-readable summary of the problem type.
     *
     * @param title the title; may be null to leave the member absent
     * @return this builder
     */
    public Builder title(String title) {
      this.title = title;
      return this;
    }

    /**
     * Sets the HTTP status code for this occurrence.
     *
     * <p>The range check is a project decision, not an RFC requirement: RFC 9457, Section 3.1.2
     * defines {@code status} only as "the HTTP status code", and this library pins that to the
     * 100–599 range of HTTP status codes.
     *
     * @param status the HTTP status code, between 100 and 599
     * @return this builder
     */
    public Builder status(int status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the human-readable explanation specific to this occurrence.
     *
     * @param detail the detail; may be null to leave the member absent
     * @return this builder
     */
    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    /**
     * Sets the URI reference identifying this specific occurrence.
     *
     * @param instance the instance URI reference; must not be null
     * @return this builder
     * @throws NullPointerException if {@code instance} is null
     */
    public Builder instance(URI instance) {
      this.instance = Objects.requireNonNull(instance, "instance");
      return this;
    }

    /**
     * Sets the URI reference identifying this specific occurrence from its string form.
     *
     * @param instance the instance URI reference, for example {@code "/account/12345/msgs/abc"};
     *     must not be null
     * @return this builder
     * @throws NullPointerException if {@code instance} is null
     * @throws IllegalArgumentException if {@code instance} is not a valid URI reference
     */
    public Builder instance(String instance) {
      return instance(URI.create(Objects.requireNonNull(instance, "instance")));
    }

    /**
     * Puts a value under the given extension key's name, replacing any previous value put under the
     * same name.
     *
     * @param key the extension key; must not be null
     * @param value the extension value; must not be null
     * @param <T> the value type bound to the key
     * @return this builder
     * @throws NullPointerException if {@code key} or {@code value} is null
     */
    public <T> Builder extension(ProblemDetailKey<T> key, T value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      extensions.put(key, value);
      return this;
    }

    /**
     * Builds an immutable problem detail from the current builder state.
     *
     * @return a new immutable problem detail
     * @throws IllegalStateException if {@code status} was set outside the 100–599 range
     */
    public ProblemDetail build() {
      if (status != null && (status < 100 || status > 599)) {
        throw new IllegalStateException(
            "status must be an HTTP status code between 100 and 599 but was " + status);
      }
      return new ProblemDetail(this);
    }
  }
}
