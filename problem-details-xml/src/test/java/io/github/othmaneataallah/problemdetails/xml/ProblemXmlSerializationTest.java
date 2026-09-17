package io.github.othmaneataallah.problemdetails.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

class ProblemXmlSerializationTest {

  @Test
  void outOfCreditExampleMatchesRfcAppendixB() throws XMLStreamException {
    ProblemDetail problem =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .detail("Your current balance is 30, but that costs 50.")
            .instance("https://example.net/account/12345/msgs/abc")
            .extension(ProblemDetailKey.of("balance", Integer.class), 30)
            .extension(
                ProblemDetailKey.of("accounts", Object.class),
                List.of("https://example.net/account/12345", "https://example.net/account/67890"))
            .build();

    assertThat(ProblemXml.toXml(problem))
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<problem xmlns=\"urn:ietf:rfc:7807\">"
                + "<type>https://example.com/probs/out-of-credit</type>"
                + "<title>You do not have enough credit.</title>"
                + "<detail>Your current balance is 30, but that costs 50.</detail>"
                + "<instance>https://example.net/account/12345/msgs/abc</instance>"
                + "<balance>30</balance>"
                + "<accounts><i>https://example.net/account/12345</i>"
                + "<i>https://example.net/account/67890</i></accounts>"
                + "</problem>");
  }

  @Test
  void absentMembersAreOmittedExceptDefaultType() throws XMLStreamException {
    assertThat(ProblemXml.toXml(ProblemDetail.builder().build()))
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<problem xmlns=\"urn:ietf:rfc:7807\">"
                + "<type>about:blank</type>"
                + "</problem>");
  }

  @Test
  void statusAndNestedExtensionsSerialize() throws XMLStreamException {
    Map<String, Object> validation = new LinkedHashMap<>();
    validation.put("pointer", "#/age");
    validation.put("limit", 18);

    ProblemDetail problem =
        ProblemDetail.builder()
            .status(422)
            .title("Your request is not valid.")
            .extension(ProblemDetailKey.of("errors", Object.class), List.of(validation))
            .build();

    assertThat(ProblemXml.toXml(problem))
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<problem xmlns=\"urn:ietf:rfc:7807\">"
                + "<type>about:blank</type>"
                + "<title>Your request is not valid.</title>"
                + "<status>422</status>"
                + "<errors><i><pointer>#/age</pointer><limit>18</limit></i></errors>"
                + "</problem>");
  }

  @Test
  void specialCharactersAreEscaped() throws XMLStreamException {
    ProblemDetail problem =
        ProblemDetail.builder().detail("Balance < 30 & falling, \"quoted\"").build();

    assertThat(ProblemXml.toXml(problem))
        .contains("<detail>Balance &lt; 30 &amp; falling, \"quoted\"</detail>");
  }

  @Test
  void unsupportedExtensionValueTypeIsRejected() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("custom", Object.class), new Object())
            .build();

    assertThatThrownBy(() -> ProblemXml.toXml(problem))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported extension value type");
  }

  @Test
  void booleanAndCharacterValuesSerializeAsText() throws XMLStreamException {
    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("flag", Boolean.class), true)
            .extension(ProblemDetailKey.of("initial", Character.class), 'x')
            .build();

    assertThat(ProblemXml.toXml(problem)).contains("<flag>true</flag><initial>x</initial>");
  }

  @Test
  void nullsInsideContainersAreOmitted() throws XMLStreamException {
    List<String> items = new ArrayList<>();
    items.add("a");
    items.add(null);
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("present", "yes");
    values.put("missing", null);

    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("items", Object.class), items)
            .extension(ProblemDetailKey.of("values", Object.class), values)
            .build();

    assertThat(ProblemXml.toXml(problem))
        .contains("<items><i>a</i></items>")
        .contains("<values><present>yes</present></values>");
  }

  @Test
  void invalidElementNameIsRejected() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("not a name", String.class), "value")
            .build();

    assertThatThrownBy(() -> ProblemXml.toXml(problem)).isInstanceOf(XMLStreamException.class);
  }

  @Test
  void nullProblemIsRejected() {
    assertThatThrownBy(() -> ProblemXml.toXml(null)).isInstanceOf(NullPointerException.class);
  }
}
