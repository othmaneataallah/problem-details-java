package io.github.othmaneataallah.problemdetails.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ProblemDetailsModuleTest {

  private final JsonMapper mapper =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  @Test
  void problemJsonMediaType() {
    assertThat(ProblemMediaTypes.PROBLEM_JSON).isEqualTo("application/problem+json");
  }

  @Test
  void roundTripPreservesEquality() {
    Map<String, Object> validation = new LinkedHashMap<>();
    validation.put("pointer", "#/age");
    validation.put("limit", 18);

    ProblemDetail original =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .status(403)
            .detail("Your current balance is 30, but that costs 50.")
            .instance("/account/12345/msgs/abc")
            .extension(ProblemDetailKey.of("balance", Integer.class), 30)
            .extension(ProblemDetailKey.of("big", Long.class), 5000000000L)
            .extension(ProblemDetailKey.of("ratio", Double.class), 0.5)
            .extension(ProblemDetailKey.of("active", Boolean.class), true)
            .extension(
                ProblemDetailKey.of("accounts", Object.class),
                List.of("/account/12345", "/account/67890"))
            .extension(ProblemDetailKey.of("validation", Object.class), validation)
            .build();

    String json = mapper.writeValueAsString(original);
    ProblemDetail readBack = mapper.readValue(json, ProblemDetail.class);

    assertThat(readBack).isEqualTo(original);
    assertThat(readBack.get(ProblemDetailKey.of("balance", Integer.class))).contains(30);
    assertThat(readBack.get(ProblemDetailKey.of("big", Long.class))).contains(5000000000L);
  }
}
