/**
 * Thin Spring MVC/WebFlux integration for RFC 9457 problem details.
 *
 * <p>Spring Framework ships native RFC 9457 support ({@code
 * org.springframework.http.ProblemDetail}, rendered with {@code application/problem+json}); this
 * module bridges the immutable model of {@code problem-details-core} into that pipeline instead of
 * re-rendering responses itself:
 *
 * <ul>
 *   <li>{@link io.github.othmaneataallah.problemdetails.spring.SpringProblemDetails} converts this
 *       library's problem details to Spring's representation and to {@code ResponseEntity},
 *   <li>{@link io.github.othmaneataallah.problemdetails.spring.ProblemDetailAdvice} is a {@code
 *       ControllerAdvice} mapping {@code ProblemDetailException} to error responses.
 * </ul>
 *
 * <p>Only {@code spring-web} types are used, so the same advice serves Spring MVC and Spring
 * WebFlux applications. Register it explicitly — component scanning does not reach into library
 * packages — for example with {@code @Import(ProblemDetailAdvice.class)}. No Spring Boot
 * auto-configuration is provided, keeping the integration thin.
 */
package io.github.othmaneataallah.problemdetails.spring;
