package io.github.othmaneataallah.problemdetails.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Typed key identifying a single problem detail extension member.
 *
 * <p>RFC 9457, Section 3.2 permits problem types to extend the problem details object with
 * additional members. Rather than exposing those members through an untyped map, this library
 * identifies each member by a {@code ProblemDetailKey} that binds the member {@linkplain #name()
 * name} to its Java {@linkplain #type() type}, so extension values are read back with static type
 * safety:
 *
 * <pre>{@code
 * ProblemDetailKey<Integer> BALANCE = ProblemDetailKey.of("balance", Integer.class);
 *
 * ProblemDetail problem = ProblemDetail.builder().extension(BALANCE, 30).build();
 * int balance = problem.get(BALANCE).orElseThrow();
 * }</pre>
 *
 * <p>Keys are compared by {@linkplain #name() name} only: the name is what identifies the member on
 * the wire, so two keys with the same name but different Java types denote the same member and
 * cannot coexist on one problem detail. The last value put under a name wins.
 *
 * <p>Instances are immutable and thread-safe.
 *
 * @param <T> the Java type of the extension member value
 */
public final class ProblemDetailKey<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String name;
  private final Class<T> type;

  private ProblemDetailKey(String name, Class<T> type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Creates a key for the extension member with the given name and Java type.
   *
   * <p>This is a project-level contract, not an RFC requirement: the RFC only requires extension
   * names to be usable as object member names, and additionally recommends they conform to the XML
   * {@code Name} rule for use with the XML format (RFC 9457, Section 3.2). Name-syntax validation
   * is left to the serialization modules; the core only rejects names that are meaningless
   * everywhere.
   *
   * @param name the extension member name, for example {@code "balance"}; must not be null or blank
   * @param type the Java type of the member value; must not be null
   * @param <T> the Java type of the member value
   * @return a new key binding {@code name} to {@code type}
   * @throws NullPointerException if {@code name} or {@code type} is null
   * @throws IllegalArgumentException if {@code name} is blank
   */
  public static <T> ProblemDetailKey<T> of(String name, Class<T> type) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    if (name.isBlank()) {
      throw new IllegalArgumentException("Extension member name must not be blank");
    }
    return new ProblemDetailKey<>(name, type);
  }

  /**
   * Returns the extension member name.
   *
   * @return the member name; never null or blank
   */
  public String name() {
    return name;
  }

  /**
   * Returns the Java type of the extension member value.
   *
   * @return the value type; never null
   */
  public Class<T> type() {
    return type;
  }

  /**
   * Casts a stored value to this key's type.
   *
   * @param value the value to cast; may be null, in which case null is returned
   * @return the value as {@code T}
   * @throws ClassCastException if the value is not of this key's type
   */
  public T cast(Object value) {
    return type.cast(value);
  }

  /**
   * Compares keys by member name only, since the name is what identifies the member on the wire.
   *
   * @param obj the object to compare with
   * @return true if {@code obj} is a key with the same name
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProblemDetailKey<?> other)) {
      return false;
    }
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "ProblemDetailKey[name=" + name + ", type=" + type.getName() + "]";
  }
}
