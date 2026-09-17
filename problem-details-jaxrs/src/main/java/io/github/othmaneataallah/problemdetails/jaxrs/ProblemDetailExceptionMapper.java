package io.github.othmaneataallah.problemdetails.jaxrs;

import static io.github.othmaneataallah.problemdetails.jackson.ProblemMediaTypes.PROBLEM_JSON;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link ProblemDetailException} to RFC 9457 error responses.
 *
 * <p>The response status comes from the carried problem detail, defaulting to 500 Internal Server
 * Error when the detail has no {@code status} member — the same project decision as the Spring
 * module, since an error response must carry a status. The body is the carried problem detail
 * itself, serialized as {@code application/problem+json} by {@link ProblemDetailMessageBodyWriter};
 * pinning JSON keeps error responses deterministic, while resource methods returning problem
 * details directly still negotiate both media types.
 */
@Provider
public final class ProblemDetailExceptionMapper implements ExceptionMapper<ProblemDetailException> {

  /** Creates a mapper. */
  public ProblemDetailExceptionMapper() {}

  @Override
  public Response toResponse(ProblemDetailException exception) {
    ProblemDetail problem = exception.getProblemDetail();
    int status = problem.getStatus() != null ? problem.getStatus() : 500;
    return Response.status(status).type(PROBLEM_JSON).entity(problem).build();
  }
}
