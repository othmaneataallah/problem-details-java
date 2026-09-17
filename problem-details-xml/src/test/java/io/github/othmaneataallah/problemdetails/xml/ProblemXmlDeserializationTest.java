package io.github.othmaneataallah.problemdetails.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

class ProblemXmlDeserializationTest {

  @Test
  void rfcAppendixBExampleParses() throws XMLStreamException {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<problem xmlns=\"urn:ietf:rfc:7807\">"
            + "<type>https://example.com/probs/out-of-credit</type>"
            + "<title>You do not have enough credit.</title>"
            + "<detail>Your current balance is 30, but that costs 50.</detail>"
            + "<instance>https://example.net/account/12345/msgs/abc</instance>"
            + "<balance>30</balance>"
            + "<accounts><i>https://example.net/account/12345</i>"
            + "<i>https://example.net/account/67890</i></accounts>"
            + "</problem>";

    ProblemDetail problem = ProblemXml.fromXml(xml);

    assertThat(problem.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
    assertThat(problem.getTitle()).isEqualTo("You do not have enough credit.");
    assertThat(problem.getStatus()).isNull();
    assertThat(problem.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
    assertThat(problem.getInstance())
        .isEqualTo(URI.create("https://example.net/account/12345/msgs/abc"));
    // The XML format carries no types: numbers travel as text and read back as strings.
    assertThat(problem.get(ProblemDetailKey.of("balance", String.class))).contains("30");
    assertThat(problem.extensionMembers())
        .containsEntry("balance", "30")
        .containsEntry(
            "accounts",
            List.of("https://example.net/account/12345", "https://example.net/account/67890"));
  }

  @Test
  void statusElementParses() throws XMLStreamException {
    assertThat(ProblemXml.fromXml(wrap("<status>403</status>")).getStatus()).isEqualTo(403);
  }

  @Test
  void mistypedStandardMembersAreIgnored() throws XMLStreamException {
    ProblemDetail problem =
        ProblemXml.fromXml(
            wrap(
                "<type>not a valid uri</type><status>wide</status><title><b>bold</b></title>"
                    + "<detail>ok</detail>"));

    assertThat(problem.getType()).isEqualTo(ProblemDetail.ABOUT_BLANK);
    assertThat(problem.getStatus()).isNull();
    assertThat(problem.getTitle()).isNull();
    assertThat(problem.getDetail()).isEqualTo("ok");
  }

  @Test
  void outOfRangeStatusIsIgnored() throws XMLStreamException {
    assertThat(ProblemXml.fromXml(wrap("<status>99</status>")).getStatus()).isNull();
    assertThat(ProblemXml.fromXml(wrap("<status>600</status>")).getStatus()).isNull();
  }

  @Test
  void prettyPrintedInputParsesCleanly() throws XMLStreamException {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<problem xmlns=\"urn:ietf:rfc:7807\">\n"
            + "  <title>You do not have enough credit.</title>\n"
            + "  <accounts>\n"
            + "    <i>/account/12345</i>\n"
            + "    <i>/account/67890</i>\n"
            + "  </accounts>\n"
            + "</problem>";

    ProblemDetail problem = ProblemXml.fromXml(xml);

    assertThat(problem.getTitle()).isEqualTo("You do not have enough credit.");
    assertThat(problem.extensionMembers())
        .containsEntry("accounts", List.of("/account/12345", "/account/67890"));
  }

  @Test
  void objectExtensionsParseToMaps() throws XMLStreamException {
    ProblemDetail problem =
        ProblemXml.fromXml(
            wrap("<validation><pointer>#/age</pointer><limit>18</limit></validation>"));

    assertThat(problem.extensionMembers())
        .containsEntry("validation", Map.of("pointer", "#/age", "limit", "18"));
  }

  @Test
  void emptyElementReadsAsEmptyString() throws XMLStreamException {
    ProblemDetail problem = ProblemXml.fromXml(wrap("<note></note>"));

    assertThat(problem.extensionMembers()).containsEntry("note", "");
  }

  @Test
  void nonProblemRootIsRejected() {
    assertThatThrownBy(() -> ProblemXml.fromXml("<html><body>nope</body></html>"))
        .isInstanceOf(XMLStreamException.class)
        .hasMessageContaining("<problem>");
  }

  @Test
  void malformedXmlIsRejected() {
    assertThatThrownBy(() -> ProblemXml.fromXml("<problem>"))
        .isInstanceOf(XMLStreamException.class);
  }

  @Test
  void externalEntitiesAreRejected() {
    String xxe =
        "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE problem [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
            + "<problem xmlns=\"urn:ietf:rfc:7807\"><title>&xxe;</title></problem>";

    assertThatThrownBy(() -> ProblemXml.fromXml(xxe)).isInstanceOf(XMLStreamException.class);
  }

  @Test
  void nullInputIsRejected() {
    assertThatThrownBy(() -> ProblemXml.fromXml((String) null))
        .isInstanceOf(NullPointerException.class);
  }

  private static String wrap(String body) {
    return "<problem xmlns=\"urn:ietf:rfc:7807\">" + body + "</problem>";
  }
}
