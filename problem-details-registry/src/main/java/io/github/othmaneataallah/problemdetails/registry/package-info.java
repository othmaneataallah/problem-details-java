/**
 * IANA and custom problem-type registry for RFC 9457 problem details.
 *
 * <p>RFC 9457, Section 4.2 defines the "HTTP Problem Types" registry of common, widely used problem
 * type URIs. The entry points here are:
 *
 * <ul>
 *   <li>{@link io.github.othmaneataallah.problemdetails.registry.ProblemType} — the immutable
 *       metadata of one problem type (type URI, title, recommended status, reference),
 *   <li>{@link io.github.othmaneataallah.problemdetails.registry.ProblemTypeRegistry} — a
 *       thread-safe registry preloaded with {@code about:blank} that also accepts custom problem
 *       types,
 *   <li>{@link io.github.othmaneataallah.problemdetails.registry.ProblemTypes} — constants for the
 *       spec-registered type and one-liner lookups.
 * </ul>
 *
 * <p>This module depends on {@code problem-details-core} only and has no third-party dependencies.
 */
package io.github.othmaneataallah.problemdetails.registry;
