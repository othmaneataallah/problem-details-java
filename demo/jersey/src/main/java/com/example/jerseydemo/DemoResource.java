package com.example.jerseydemo;

import static io.github.othmaneataallah.problemdetails.jackson.ProblemMediaTypes.PROBLEM_JSON;
import static io.github.othmaneataallah.problemdetails.xml.ProblemMediaTypes.PROBLEM_XML;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import io.github.othmaneataallah.problemdetails.registry.ProblemType;
import io.github.othmaneataallah.problemdetails.registry.ProblemTypeRegistry;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One endpoint per case, so every behavior is curl-verifiable (see verify.sh). */
@Path("demo")
public class DemoResource {

  private static final ProblemDetailKey<Integer> BALANCE =
      ProblemDetailKey.of("balance", Integer.class);

  private final ProblemTypeRegistry registry = new ProblemTypeRegistry();

  public DemoResource() {
    registry.register(
        ProblemType.of(
            "https://example.com/probs/out-of-credit",
            "You do not have enough credit.",
            403,
            "https://example.com/docs/probs"));
  }

  private ProblemDetail outOfCredit() {
    ProblemType type =
        registry.lookup("https://example.com/probs/out-of-credit").orElseThrow();
    return ProblemDetail.builder()
        .type(type.getType())
        .title(type.getTitle())
        .status(type.getRecommendedStatus())
        .detail("Your current balance is 30, but that costs 50.")
        .extension(BALANCE, 30)
        .build();
  }

  /** Control case: no problem involved. */
  @GET
  @Path("ok")
  @Produces("text/plain")
  public String ok() {
    return "ok";
  }

  /** Returned directly: serializes via the writer with content negotiation. */
  @GET
  @Path("credit")
  @Produces({PROBLEM_JSON, PROBLEM_XML})
  public ProblemDetail credit() {
    return outOfCredit();
  }

  /** Thrown: the mapper answers JSON regardless of Accept. */
  @GET
  @Path("fail")
  @Produces({PROBLEM_JSON, PROBLEM_XML})
  public String fail() {
    throw new ProblemDetailException(outOfCredit());
  }

  /** No status member: the mapper answers 500 with a faithful body. */
  @GET
  @Path("boom")
  @Produces({PROBLEM_JSON, PROBLEM_XML})
  public String boom() {
    throw new ProblemDetailException(ProblemDetail.builder().title("Boom.").build());
  }

  /** Validation-style extension, negotiable like everything returned. */
  @GET
  @Path("validate")
  @Produces({PROBLEM_JSON, PROBLEM_XML})
  public ProblemDetail validate() {
    Map<String, Object> error = new LinkedHashMap<>();
    error.put("detail", "must be a positive integer");
    error.put("pointer", "#/age");
    return ProblemDetail.builder()
        .type("https://example.net/validation-error")
        .title("Your request is not valid.")
        .status(422)
        .extension(ProblemDetailKey.of("errors", Object.class), List.of(error))
        .build();
  }

  /** Exposes the registry itself, ordered by type URI. */
  @GET
  @Path("types")
  @Produces(PROBLEM_JSON)
  public Response types() {
    List<Map<String, Object>> view =
        registry.registeredTypes().stream()
            .map(
                type -> {
                  Map<String, Object> entry = new LinkedHashMap<>();
                  entry.put("type", type.getType().toString());
                  entry.put("title", type.getTitle());
                  return entry;
                })
            .toList();
    return Response.ok(view, PROBLEM_JSON).build();
  }
}
