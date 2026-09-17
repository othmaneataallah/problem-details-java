package io.github.othmaneataallah.problemdetails.jackson;

/** Media type constants for problem details serialization. */
public final class ProblemMediaTypes {

  /**
   * The media type identifying the JSON problem details format (RFC 9457, which registers {@code
   * application/problem+json} for the canonical model of Section 3).
   */
  public static final String PROBLEM_JSON = "application/problem+json";

  private ProblemMediaTypes() {}
}
