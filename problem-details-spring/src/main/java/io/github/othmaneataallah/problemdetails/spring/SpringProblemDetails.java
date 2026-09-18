package io.github.othmaneataallah.problemdetails.spring;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Converts this library's problem details to Spring's RFC 9457 representation.
 *
 * <p>Spring Framework natively renders its own {@code org.springframework.http.ProblemDetail} with
 * {@code application/problem+json}. These helpers translate the immutable core model — including
 * typed extension members, which are copied into Spring's properties map — so applications keep one
 * model while Spring handles content negotiation and rendering.
 */
public final class SpringProblemDetails {

  private static final String PROBLEM_PARAMETER = "problem";

  private SpringProblemDetails() {}

  /**
   * Maps a problem detail to its HTTP status code.
   *
   * <p>A problem detail without a {@code status} member (valid outside an HTTP context) maps to 500
   * Internal Server Error. That fallback is a project decision, not an RFC requirement: the RFC
   * defines no status for status-less problems, while an error response must carry one.
   *
   * @param problem the problem detail; must not be null
   * @return the status code; never null
   * @throws NullPointerException if {@code problem} is null
   */
  public static HttpStatusCode statusCode(ProblemDetail problem) {
    Objects.requireNonNull(problem, PROBLEM_PARAMETER);
    if (problem.getStatus() == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return HttpStatusCode.valueOf(problem.getStatus());
  }

  /**
   * Converts a problem detail to Spring's representation.
   *
   * @param problem the problem detail; must not be null
   * @return the equivalent Spring problem detail; never null
   * @throws NullPointerException if {@code problem} is null
   */
  public static org.springframework.http.ProblemDetail toSpringDetail(ProblemDetail problem) {
    Objects.requireNonNull(problem, PROBLEM_PARAMETER);
    org.springframework.http.ProblemDetail springDetail =
        org.springframework.http.ProblemDetail.forStatus(statusCode(problem));
    springDetail.setType(problem.getType());
    if (problem.getTitle() != null) {
      springDetail.setTitle(problem.getTitle());
    }
    if (problem.getDetail() != null) {
      springDetail.setDetail(problem.getDetail());
    }
    if (problem.getInstance() != null) {
      springDetail.setInstance(problem.getInstance());
    }
    if (!problem.extensionMembers().isEmpty()) {
      // Spring initializes the properties map lazily, so it must be replaced rather than
      // added to when extensions are present.
      springDetail.setProperties(new LinkedHashMap<>(problem.extensionMembers()));
    }
    return springDetail;
  }

  /**
   * Converts a problem detail to a response entity with an {@code application/problem+json} content
   * type.
   *
   * @param problem the problem detail; must not be null
   * @return the response entity; never null
   * @throws NullPointerException if {@code problem} is null
   */
  public static ResponseEntity<org.springframework.http.ProblemDetail> toResponseEntity(
      ProblemDetail problem) {
    Objects.requireNonNull(problem, PROBLEM_PARAMETER);
    return ResponseEntity.status(statusCode(problem))
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(toSpringDetail(problem));
  }
}
