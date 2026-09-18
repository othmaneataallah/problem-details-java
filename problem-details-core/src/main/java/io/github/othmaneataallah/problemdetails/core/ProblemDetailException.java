package io.github.othmaneataallah.problemdetails.core;

import java.util.Objects;

/**
 * A runtime exception carrying an RFC 9457 problem detail.
 *
 * <p>Throwing a problem detail directly lets error-handling code — and later the framework
 * integrations — recover the structured {@link ProblemDetail} instead of parsing a message string,
 * which mirrors the RFC's own advice that consumers should not parse the human-readable {@code
 * detail} member (RFC 9457, Section 3.1.4).
 *
 * <p>The exception message is derived from the problem detail: the {@code detail} member when
 * present, otherwise the {@code title}, otherwise the {@code type} identifier, so the exception
 * stays informative in plain stack traces and logs.
 *
 * <p>Subclassing is permitted for framework integrations or application-specific handling; the
 * carried problem detail is fixed at construction time.
 */
public class ProblemDetailException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private static final String PROBLEM_DETAIL_PARAMETER = "problemDetail";

  private final ProblemDetail problemDetail;

  /**
   * Creates an exception carrying the given problem detail.
   *
   * @param problemDetail the problem detail; must not be null
   * @throws NullPointerException if {@code problemDetail} is null
   */
  public ProblemDetailException(ProblemDetail problemDetail) {
    super(messageFor(problemDetail));
    this.problemDetail = Objects.requireNonNull(problemDetail, PROBLEM_DETAIL_PARAMETER);
  }

  /**
   * Creates an exception carrying the given problem detail caused by the given throwable.
   *
   * @param problemDetail the problem detail; must not be null
   * @param cause the cause; may be null
   * @throws NullPointerException if {@code problemDetail} is null
   */
  public ProblemDetailException(ProblemDetail problemDetail, Throwable cause) {
    super(messageFor(problemDetail), cause);
    this.problemDetail = Objects.requireNonNull(problemDetail, PROBLEM_DETAIL_PARAMETER);
  }

  /**
   * Returns the carried problem detail.
   *
   * @return the problem detail; never null
   */
  public ProblemDetail getProblemDetail() {
    return problemDetail;
  }

  private static String messageFor(ProblemDetail problemDetail) {
    Objects.requireNonNull(problemDetail, PROBLEM_DETAIL_PARAMETER);
    if (problemDetail.getDetail() != null) {
      return problemDetail.getDetail();
    }
    if (problemDetail.getTitle() != null) {
      return problemDetail.getTitle();
    }
    return problemDetail.getType().toString();
  }
}
