package io.github.othmaneataallah.problemdetails.xml;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Converts {@link ProblemDetail} to and from the XML format of RFC 9457, Appendix B.
 *
 * <p>The format is a {@code <problem>} root element in the {@code urn:ietf:rfc:7807} namespace
 * (kept from the obsoleted RFC 7807) with one child element per member. Extension members follow
 * the appendix's convention: an element containing child elements represents an object, except an
 * element containing only {@code <i>} children, which represents an array.
 *
 * <p>Two asymmetries with the in-memory model follow directly from the format and are deliberate:
 *
 * <ul>
 *   <li>all values travel as text, so extension scalars always read back as {@code String} — no
 *       type guessing is applied, which also keeps values like {@code "01234"} intact;
 *   <li>empty containers have no representation, so an empty list or map writes as an empty element
 *       and reads back as an empty string.
 * </ul>
 *
 * <p>Reading is otherwise lenient, mirroring the JSON module: unparseable {@code type} or {@code
 * instance} URIs, non-numeric or out-of-range {@code status} values, and unknown elements in place
 * of standard members are ignored, while unknown elements elsewhere become extensions. Scalar text
 * is trimmed, since insignificant whitespace is unavoidable in XML. Callers supplying their own
 * {@link XMLStreamReader} are responsible for configuring it securely; the {@link #fromXml(String)}
 * overload disables DTDs and external entities.
 */
public final class ProblemXml {

  /**
   * The XML namespace of the problem details format, kept from RFC 7807 by RFC 9457, Appendix B.
   */
  public static final String NAMESPACE = "urn:ietf:rfc:7807";

  /** Local name of the format's root element. */
  private static final String ROOT_ELEMENT = "problem";

  private static final String PROBLEM_PARAMETER = "problem";

  private ProblemXml() {}

  /**
   * Serializes a problem detail to its XML representation, including the XML declaration.
   *
   * @param problem the problem detail; must not be null
   * @return the XML document; never null
   * @throws NullPointerException if {@code problem} is null
   * @throws IllegalArgumentException if an extension value is not a String, Number, Boolean,
   *     Character, List, or Map
   * @throws XMLStreamException if writing fails, including on extension names that are not valid
   *     XML element names
   */
  public static String toXml(ProblemDetail problem) throws XMLStreamException {
    StringWriter out = new StringWriter();
    toXml(problem, XMLOutputFactory.newFactory().createXMLStreamWriter(out));
    return out.toString();
  }

  /**
   * Serializes a problem detail to the given stream writer, without an XML declaration.
   *
   * <p>The writer is flushed but not closed.
   *
   * @param problem the problem detail; must not be null
   * @param out the stream writer; must not be null
   * @throws NullPointerException if {@code problem} or {@code out} is null
   * @throws IllegalArgumentException if an extension value is not a String, Number, Boolean,
   *     Character, List, or Map
   * @throws XMLStreamException if writing fails, including on extension names that are not valid
   *     XML element names
   */
  public static void toXml(ProblemDetail problem, XMLStreamWriter out) throws XMLStreamException {
    Objects.requireNonNull(problem, PROBLEM_PARAMETER);
    Objects.requireNonNull(out, "out");
    out.writeStartDocument("UTF-8", "1.0");
    out.writeStartElement(ROOT_ELEMENT);
    out.writeDefaultNamespace(NAMESPACE);
    writeTextElement(out, "type", problem.getType().toString());
    writeTextElement(out, "title", problem.getTitle());
    writeTextElement(out, "detail", problem.getDetail());
    if (problem.getStatus() != null) {
      writeTextElement(out, "status", problem.getStatus().toString());
    }
    if (problem.getInstance() != null) {
      writeTextElement(out, "instance", problem.getInstance().toString());
    }
    for (Map.Entry<String, Object> extension : problem.extensionMembers().entrySet()) {
      writeExtension(out, extension.getKey(), extension.getValue());
    }
    out.writeEndElement();
    out.writeEndDocument();
    out.flush();
  }

  /**
   * Parses a problem detail from its XML representation.
   *
   * <p>DTDs and external entities are disabled.
   *
   * @param xml the XML document; must not be null
   * @return the parsed problem detail; never null
   * @throws NullPointerException if {@code xml} is null
   * @throws XMLStreamException if parsing fails or the root element is not a {@code problem}
   *     element
   */
  public static ProblemDetail fromXml(String xml) throws XMLStreamException {
    Objects.requireNonNull(xml, "xml");
    // The factory never returns null (it throws instead), but its signature carries no
    // nullness contract for analyzers.
    XMLStreamReader in =
        Objects.requireNonNull(
            newInputFactory().createXMLStreamReader(new StringReader(xml)), "reader");
    try {
      return fromXml(in);
    } finally {
      in.close();
    }
  }

  /**
   * Parses a problem detail from the given stream reader, which must be positioned before the
   * {@code problem} root element.
   *
   * @param in the stream reader; must not be null
   * @return the parsed problem detail; never null
   * @throws NullPointerException if {@code in} is null
   * @throws XMLStreamException if parsing fails or the root element is not a {@code problem}
   *     element
   */
  public static ProblemDetail fromXml(XMLStreamReader in) throws XMLStreamException {
    Objects.requireNonNull(in, "in");
    moveToRootElement(in);
    ProblemDetail.Builder builder = ProblemDetail.builder();
    while (in.hasNext()) {
      int event = in.next();
      if (event == START_ELEMENT) {
        readMember(in, builder);
      } else if (event == END_ELEMENT) {
        return builder.build();
      }
    }
    throw new XMLStreamException("Unexpected end of input inside <problem>");
  }

  /** Consumes events up to the {@code problem} root element, rejecting anything else. */
  private static void moveToRootElement(XMLStreamReader in) throws XMLStreamException {
    boolean found = false;
    while (in.hasNext()) {
      if (in.next() == START_ELEMENT) {
        found = true;
        break;
      }
    }
    if (!found || !ROOT_ELEMENT.equals(in.getLocalName())) {
      throw new XMLStreamException("Expected a <problem> root element");
    }
  }

  /** Reads the member the reader is positioned on into the builder. */
  private static void readMember(XMLStreamReader in, ProblemDetail.Builder builder)
      throws XMLStreamException {
    String name = in.getLocalName();
    switch (name) {
      case "type" -> readUriMember(in, builder, ProblemDetail.Builder::type);
      case "status" -> readStatusMember(in, builder);
      case "title" -> readTextMember(in, builder, ProblemDetail.Builder::title);
      case "detail" -> readTextMember(in, builder, ProblemDetail.Builder::detail);
      case "instance" -> readUriMember(in, builder, ProblemDetail.Builder::instance);
      default -> putExtension(builder, name, parseElement(in));
    }
  }

  /**
   * Reads a URI member, ignoring unparseable values. An ignored {@code type} falls back to the core
   * default; an ignored {@code instance} stays absent.
   */
  private static void readUriMember(
      XMLStreamReader in,
      ProblemDetail.Builder builder,
      BiConsumer<ProblemDetail.Builder, URI> setter)
      throws XMLStreamException {
    Object value = parseElement(in);
    if (value instanceof String text) {
      try {
        setter.accept(builder, URI.create(text));
      } catch (IllegalArgumentException ignored) {
        // Fall through, mirroring the JSON module.
      }
    }
  }

  /** Reads the {@code status} member, ignoring non-numeric and out-of-range values. */
  private static void readStatusMember(XMLStreamReader in, ProblemDetail.Builder builder)
      throws XMLStreamException {
    Object value = parseElement(in);
    if (value instanceof String text) {
      try {
        int status = Integer.parseInt(text);
        if (status >= 100 && status <= 599) {
          builder.status(status);
        }
      } catch (NumberFormatException ignored) {
        // Fall through to absent, mirroring the JSON module.
      }
    }
  }

  /** Reads a plain text member, ignoring markup content. */
  private static void readTextMember(
      XMLStreamReader in,
      ProblemDetail.Builder builder,
      BiConsumer<ProblemDetail.Builder, String> setter)
      throws XMLStreamException {
    Object value = parseElement(in);
    if (value instanceof String text) {
      setter.accept(builder, text);
    }
  }

  private static void writeTextElement(XMLStreamWriter out, String name, String value)
      throws XMLStreamException {
    if (value == null) {
      return;
    }
    out.writeStartElement(NAMESPACE, name);
    out.writeCharacters(value);
    out.writeEndElement();
  }

  private static void writeExtension(XMLStreamWriter out, String name, Object value)
      throws XMLStreamException {
    requireValidElementName(name);
    if (value instanceof String
        || value instanceof Number
        || value instanceof Boolean
        || value instanceof Character) {
      writeTextElement(out, name, value.toString());
    } else if (value instanceof List<?> list) {
      out.writeStartElement(NAMESPACE, name);
      for (Object item : list) {
        if (item != null) {
          writeExtension(out, "i", item);
        }
      }
      out.writeEndElement();
    } else if (value instanceof Map<?, ?> map) {
      out.writeStartElement(NAMESPACE, name);
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getValue() != null) {
          writeExtension(out, String.valueOf(entry.getKey()), entry.getValue());
        }
      }
      out.writeEndElement();
    } else {
      throw new IllegalArgumentException(
          "Unsupported extension value type for XML serialization: " + value.getClass().getName());
    }
  }

  /**
   * Creates securely configured reader factories: DTDs and external entities stay disabled
   * everywhere, including validation probes, so untrusted input can never resolve them.
   */
  private static XMLInputFactory newInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(XMLInputFactory.IS_COALESCING, true);
    return factory;
  }

  /**
   * Rejects names the writer would otherwise emit as malformed markup. The check parses a probe
   * element, so exactly the XML {@code Name} production is enforced without reimplementing it.
   */
  private static void requireValidElementName(String name) throws XMLStreamException {
    try {
      XMLStreamReader probe =
          newInputFactory().createXMLStreamReader(new StringReader("<" + name + "/>"));
      try {
        while (probe.hasNext()) {
          probe.next();
        }
      } finally {
        probe.close();
      }
    } catch (XMLStreamException e) {
      throw new XMLStreamException(
          "Invalid XML element name for extension member: \"" + name + "\"", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void putExtension(ProblemDetail.Builder builder, String name, Object value) {
    // Safe: the key only records the value's runtime type while lookups match by member name,
    // so the cast can never cause a heap-pollution failure for the stored value itself.
    Class<Object> type = (Class<Object>) value.getClass();
    builder.extension(ProblemDetailKey.of(name, type), value);
  }

  /**
   * Parses the element the reader is positioned on, consuming through its end element. Elements
   * containing child elements become containers (maps, or lists when every child is named {@code
   * i}, per Appendix B); anything else becomes trimmed text.
   */
  private static Object parseElement(XMLStreamReader in) throws XMLStreamException {
    StringBuilder text = new StringBuilder();
    List<Map.Entry<String, Object>> children = null;
    while (in.hasNext()) {
      int event = in.next();
      switch (event) {
        case CHARACTERS, CDATA, SPACE -> text.append(in.getText());
        case START_ELEMENT -> {
          if (children == null) {
            children = new ArrayList<>();
          }
          children.add(Map.entry(in.getLocalName(), parseElement(in)));
        }
        case END_ELEMENT -> {
          if (children == null) {
            return text.toString().trim();
          }
          boolean array = children.stream().allMatch(child -> child.getKey().equals("i"));
          if (array) {
            List<Object> values = new ArrayList<>();
            children.forEach(child -> values.add(child.getValue()));
            return values;
          }
          Map<String, Object> values = new LinkedHashMap<>();
          children.forEach(child -> values.put(child.getKey(), child.getValue()));
          return values;
        }
        default -> {
          // Comments, processing instructions, and other non-element events carry no data.
        }
      }
    }
    throw new XMLStreamException("Unexpected end of input inside an element");
  }
}
