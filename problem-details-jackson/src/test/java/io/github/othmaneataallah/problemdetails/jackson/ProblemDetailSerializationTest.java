package io.github.othmaneataallah.problemdetails.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ProblemDetailSerializationTest {

  private final JsonMapper mapper =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  @Test
  void outOfCreditExampleMatchesRfc() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .detail("Your current balance is 30, but that costs 50.")
            .instance("/account/12345/msgs/abc")
            .extension(ProblemDetailKey.of("balance", Integer.class), 30)
            .extension(
                ProblemDetailKey.of("accounts", Object.class),
                List.of("/account/12345", "/account/67890"))
            .build();

    assertThat(mapper.writeValueAsString(problem))
        .isEqualTo(
            "{\"type\":\"https://example.com/probs/out-of-credit\","
                + "\"title\":\"You do not have enough credit.\","
                + "\"detail\":\"Your current balance is 30, but that costs 50.\","
                + "\"instance\":\"/account/12345/msgs/abc\","
                + "\"balance\":30,"
                + "\"accounts\":[\"/account/12345\",\"/account/67890\"]}");
  }

  @Test
  void validationErrorExampleMatchesRfc() {
    Map<String, Object> ageError = new LinkedHashMap<>();
    ageError.put("detail", "must be a positive integer");
    ageError.put("pointer", "#/age");
    Map<String, Object> colorError = new LinkedHashMap<>();
    colorError.put("detail", "must be 'green', 'red' or 'blue'");
    colorError.put("pointer", "#/profile/color");

    ProblemDetail problem =
        ProblemDetail.builder()
            .type("https://example.net/validation-error")
            .title("Your request is not valid.")
            .extension(ProblemDetailKey.of("errors", Object.class), List.of(ageError, colorError))
            .build();

    assertThat(mapper.writeValueAsString(problem))
        .isEqualTo(
            "{\"type\":\"https://example.net/validation-error\","
                + "\"title\":\"Your request is not valid.\","
                + "\"errors\":[{\"detail\":\"must be a positive integer\",\"pointer\":\"#/age\"},"
                + "{\"detail\":\"must be 'green', 'red' or 'blue'\",\"pointer\":\"#/profile/color\"}]}");
  }

  @Test
  void absentMembersAreOmittedExceptDefaultType() {
    assertThat(mapper.writeValueAsString(ProblemDetail.builder().build()))
        .isEqualTo("{\"type\":\"about:blank\"}");
  }

  @Test
  void statusIsWrittenWhenPresent() {
    ProblemDetail problem = ProblemDetail.builder().status(404).title("Not found.").build();

    assertThat(mapper.writeValueAsString(problem))
        .isEqualTo("{\"type\":\"about:blank\",\"status\":404,\"title\":\"Not found.\"}");
  }

  @Test
  void extensionInsertionOrderIsPreserved() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("zeta", Integer.class), 1)
            .extension(ProblemDetailKey.of("alpha", Integer.class), 2)
            .build();

    assertThat(mapper.writeValueAsString(problem))
        .isEqualTo("{\"type\":\"about:blank\",\"zeta\":1,\"alpha\":2}");
  }

  @Test
  void nestedStructuresSerialize() {
    Map<String, Object> nested = new LinkedHashMap<>();
    nested.put("count", 3);
    nested.put("tags", new ArrayList<>(List.of("a", "b")));

    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(ProblemDetailKey.of("nested", Object.class), nested)
            .build();

    assertThat(mapper.writeValueAsString(problem))
        .isEqualTo("{\"type\":\"about:blank\",\"nested\":{\"count\":3,\"tags\":[\"a\",\"b\"]}}");
  }
}
