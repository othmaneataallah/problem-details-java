/**
 * JAX-RS integration for RFC 9457 problem details.
 *
 * <p>The entry points are:
 *
 * <ul>
 *   <li>{@link io.github.othmaneataallah.problemdetails.jaxrs.ProblemDetailMessageBodyWriter} —
 *       serializes problem details as {@code application/problem+json} or {@code
 *       application/problem+xml} using the Jackson and XML modules,
 *   <li>{@link io.github.othmaneataallah.problemdetails.jaxrs.ProblemDetailExceptionMapper} — maps
 *       {@code ProblemDetailException} to error responses.
 * </ul>
 *
 * <p>Both are standard JAX-RS providers: register them on the application, for example via an
 * {@code Application} subclass or a registration mechanism of the JAX-RS implementation in use. The
 * Jakarta REST API dependency is {@code provided} scope — the runtime supplies it.
 */
package io.github.othmaneataallah.problemdetails.jaxrs;
