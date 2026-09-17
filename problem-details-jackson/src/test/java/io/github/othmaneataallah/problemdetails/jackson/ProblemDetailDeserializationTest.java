package io.github.othmaneataallah.problemdetails.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

class ProblemDetailDeserializationTest {

  private final JsonMapper mapper =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  @Test
  void outOfCreditExampleParses() {
    String json =
        "{\"type\":\"https://example.com/probs/out-of-credit\","
            + "\"title\":\"You do not have enough credit.\","
            + "\"detail\":\"Your current balance is 30, but that costs 50.\","
            + "\"instance\":\"/account/12345/msgs/abc\","
            + "\"balance\":30,"
            + "\"accounts\":[\"/account/12345\",\"/account/67890\"]}";

    ProblemDetail problem = mapper.readValue(json, ProblemDetail.class);

    assertThat(problem.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
    assertThat(problem.getTitle()).isEqualTo("You do not have enough credit.");
    assertThat(problem.getStatus()).isNull();
    assertThat(problem.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
    assertThat(problem.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
    assertThat(problem.get(ProblemDetailKey.of("balance", Integer.class))).contains(30);
    assertThat(problem.extensionMembers())
        .containsEntry("balance", 30)
        .containsEntry("accounts", List.of("/account/12345", "/account/67890"));
  }

  @Test
  void minimalDocumentDefaultsTypeToAboutBlank() {
    ProblemDetail problem =
        mapper.readValue("{\"title\":\"Something broke.\"}", ProblemDetail.class);

    assertThat(problem.getType()).isEqualTo(ProblemDetail.ABOUT_BLANK);
    assertThat(problem.getTitle()).isEqualTo("Something broke.");
  }

  @Test
  void mistypedStandardMembersAreIgnored() {
    ProblemDetail problem =
        mapper.readValue(
            "{\"status\":\"403\",\"title\":42,\"detail\":true,\"type\":7,\"instance\":[]}",
            ProblemDetail.class);

    assertThat(problem.getStatus()).isNull();
    assertThat(problem.getTitle()).isNull();
    assertThat(problem.getDetail()).isNull();
    assertThat(problem.getType()).isEqualTo(ProblemDetail.ABOUT_BLANK);
    assertThat(problem.getInstance()).isNull();
  }

  @Test
  void outOfRangeAndFractionalStatusAreIgnored() {
    assertThat(mapper.readValue("{\"status\":99}", ProblemDetail.class).getStatus()).isNull();
    assertThat(mapper.readValue("{\"status\":600}", ProblemDetail.class).getStatus()).isNull();
    assertThat(mapper.readValue("{\"status\":403.0}", ProblemDetail.class).getStatus()).isNull();
    assertThat(mapper.readValue("{\"status\":403}", ProblemDetail.class).getStatus())
        .isEqualTo(403);
  }

  @Test
  void invalidTypeUriFallsBackToAboutBlank() {
    ProblemDetail problem = mapper.readValue("{\"type\":\"not a valid uri\"}", ProblemDetail.class);

    assertThat(problem.getType()).isEqualTo(ProblemDetail.ABOUT_BLANK);
  }

  @Test
  void nullMembersAreTreatedAsAbsent() {
    ProblemDetail problem =
        mapper.readValue(
            "{\"title\":null,\"detail\":null,\"status\":null,\"instance\":null,\"extra\":null}",
            ProblemDetail.class);

    assertThat(problem.getTitle()).isNull();
    assertThat(problem.getDetail()).isNull();
    assertThat(problem.getStatus()).isNull();
    assertThat(problem.getInstance()).isNull();
    assertThat(problem.has(ProblemDetailKey.of("extra", Object.class))).isFalse();
    assertThat(problem.extensionMembers()).isEmpty();
  }

  @Test
  void extensionValuesMapToJavaTypes() {
    ProblemDetail problem =
        mapper.readValue(
            "{\"small\":30,\"big\":5000000000,\"ratio\":0.5,\"flag\":true,"
                + "\"name\":\"x\",\"list\":[1,\"two\"],\"obj\":{\"a\":1}}",
            ProblemDetail.class);

    assertThat(problem.extensionMembers())
        .containsEntry("small", 30)
        .containsEntry("big", 5000000000L)
        .containsEntry("ratio", 0.5)
        .containsEntry("flag", true)
        .containsEntry("name", "x")
        .containsEntry("list", List.of(1, "two"))
        .containsEntry("obj", Map.of("a", 1));
  }

  @Test
  void nonObjectDocumentIsRejected() {
    assertThatThrownBy(() -> mapper.readValue("\"just a string\"", ProblemDetail.class))
        .isInstanceOf(JacksonException.class);
    assertThatThrownBy(() -> mapper.readValue("[1,2]", ProblemDetail.class))
        .isInstanceOf(JacksonException.class);
  }
}
