package io.github.othmaneataallah.problemdetails.xml;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

class ProblemXmlTest {

  @Test
  void problemXmlMediaType() {
    assertThat(ProblemMediaTypes.PROBLEM_XML).isEqualTo("application/problem+xml");
  }

  @Test
  void roundTripPreservesStringValuedProblems() throws XMLStreamException {
    ProblemDetail original =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .status(403)
            .detail("Your current balance is 30, but that costs 50.")
            .instance("/account/12345/msgs/abc")
            .extension(ProblemDetailKey.of("balance", String.class), "30")
            .extension(
                ProblemDetailKey.of("accounts", Object.class),
                List.of("/account/12345", "/account/67890"))
            .build();

    assertThat(ProblemXml.fromXml(ProblemXml.toXml(original))).isEqualTo(original);
  }

  @Test
  void numbersReadBackAsStrings() throws XMLStreamException {
    ProblemDetail original =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("balance", Integer.class), 30)
            .build();

    ProblemDetail readBack = ProblemXml.fromXml(ProblemXml.toXml(original));

    assertThat(readBack).isNotEqualTo(original);
    assertThat(readBack.extensionMembers()).containsEntry("balance", "30");
  }
}
