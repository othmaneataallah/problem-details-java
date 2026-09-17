/**
 * XML serialization for RFC 9457 problem details using only the JDK.
 *
 * <p>The entry point is {@link io.github.othmaneataallah.problemdetails.xml.ProblemXml}, which
 * converts between {@code ProblemDetail} and the XML format of RFC 9457, Appendix B:
 *
 * <pre>{@code
 * String xml = ProblemXml.toXml(problem);
 * ProblemDetail readBack = ProblemXml.fromXml(xml);
 * }</pre>
 *
 * <p>The wire format is identified by the {@code application/problem+xml} media type (see {@link
 * io.github.othmaneataallah.problemdetails.xml.ProblemMediaTypes#PROBLEM_XML
 * ProblemMediaTypes#PROBLEM_XML}). This module has no third-party dependencies; XML reading and
 * writing use the streaming API ({@code javax.xml.stream}) built into the JDK.
 */
package io.github.othmaneataallah.problemdetails.xml;
