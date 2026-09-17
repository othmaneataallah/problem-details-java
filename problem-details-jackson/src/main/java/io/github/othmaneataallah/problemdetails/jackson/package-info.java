/**
 * JSON serialization for RFC 9457 problem details using Jackson 3.
 *
 * <p>The entry point is {@link
 * io.github.othmaneataallah.problemdetails.jackson.ProblemDetailsModule}, which registers the
 * serializer and deserializer on any Jackson {@code JsonMapper}:
 *
 * <pre>{@code
 * JsonMapper mapper =
 *     JsonMapper.builder().addModule(new ProblemDetailsModule()).build();
 *
 * String json = mapper.writeValueAsString(problem);
 * ProblemDetail readBack = mapper.readValue(json, ProblemDetail.class);
 * }</pre>
 *
 * <p>The wire format follows the canonical JSON model of RFC 9457, Section 3 and is identified by
 * the {@code application/problem+json} media type (see {@link
 * io.github.othmaneataallah.problemdetails.jackson.ProblemMediaTypes#PROBLEM_JSON
 * ProblemMediaTypes#PROBLEM_JSON}). Absent members are omitted; the {@code type} member is always
 * written since the core model defaults it to {@code about:blank}.
 */
package io.github.othmaneataallah.problemdetails.jackson;
