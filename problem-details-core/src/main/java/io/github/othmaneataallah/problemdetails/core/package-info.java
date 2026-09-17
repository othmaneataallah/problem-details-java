/**
 * Dependency-free model and API for RFC 9457, <em>Problem Details for HTTP APIs</em>.
 *
 * <p>The entry points are:
 *
 * <ul>
 *   <li>{@link io.github.othmaneataallah.problemdetails.core.ProblemDetail} — the immutable problem
 *       details model with its {@code Builder},
 *   <li>{@link io.github.othmaneataallah.problemdetails.core.ProblemDetailKey} — typed keys for
 *       problem-type-specific extension members (RFC 9457, Section 3.2),
 *   <li>{@link io.github.othmaneataallah.problemdetails.core.ProblemDetailException} — a runtime
 *       exception carrying a problem detail.
 * </ul>
 *
 * <p>This package has no runtime dependencies and is usable from a plain Java application with no
 * web framework and no serialization library on the classpath. JSON, XML, registry, and framework
 * integrations live in separate modules built on top of this API.
 */
package io.github.othmaneataallah.problemdetails.core;
