package io.github.othmaneataallah.problemdetails.spring;

import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Maps {@link ProblemDetailException} to RFC 9457 error responses.
 *
 * <p>Only {@code spring-web} types are used, so the same advice serves Spring MVC and Spring
 * WebFlux applications. Register it explicitly, for example with
 * {@code @Import(ProblemDetailAdvice.class)} — component scanning does not reach into library
 * packages, and no Spring Boot auto-configuration is provided.
 *
 * <p>The response status comes from the carried problem detail ({@link
 * SpringProblemDetails#statusCode}, defaulting to 500 when the detail has no {@code status} member)
 * and the body is the converted Spring problem detail rendered as {@code application/problem+json}.
 * As in Spring's own pipeline, an absent {@code instance} member is defaulted from the request
 * path; an explicitly set one passes through untouched.
 */
@ControllerAdvice
public class ProblemDetailAdvice {

  /** Creates the advice. */
  public ProblemDetailAdvice() {}

  /**
   * Handles a problem detail exception.
   *
   * @param exception the exception; never null when invoked by Spring
   * @return the error response; never null
   */
  @ExceptionHandler(ProblemDetailException.class)
  public ResponseEntity<org.springframework.http.ProblemDetail> handleProblemDetail(
      ProblemDetailException exception) {
    return SpringProblemDetails.toResponseEntity(exception.getProblemDetail());
  }
}
