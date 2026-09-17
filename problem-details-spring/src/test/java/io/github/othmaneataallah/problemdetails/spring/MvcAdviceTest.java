package io.github.othmaneataallah.problemdetails.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import io.github.othmaneataallah.problemdetails.jackson.ProblemDetailsModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

class MvcAdviceTest {

  private static final ProblemDetail OUT_OF_CREDIT =
      ProblemDetail.builder()
          .type("https://example.com/probs/out-of-credit")
          .title("You do not have enough credit.")
          .status(403)
          .detail("Your current balance is 30, but that costs 50.")
          .instance("https://example.com/instances/1")
          .extension(ProblemDetailKey.of("balance", Integer.class), 30)
          .build();

  private static final JsonMapper MAPPER =
      JsonMapper.builder().addModule(new ProblemDetailsModule()).build();

  @RestController
  static class CreditController {

    @GetMapping("/credit")
    public String credit() {
      throw new ProblemDetailException(OUT_OF_CREDIT);
    }

    @GetMapping("/boom")
    public String boom() {
      throw new ProblemDetailException(ProblemDetail.builder().title("Boom.").build());
    }
  }

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CreditController())
            .setControllerAdvice(new ProblemDetailAdvice())
            .build();
  }

  @Test
  void exceptionRendersProblemJsonError() throws Exception {
    String body =
        mockMvc
            .perform(get("/credit"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(MAPPER.readValue(body, ProblemDetail.class)).isEqualTo(OUT_OF_CREDIT);
  }

  @Test
  void statuslessProblemRendersInternalServerError() throws Exception {
    String body =
        mockMvc
            .perform(get("/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(MAPPER.readValue(body, ProblemDetail.class))
        .isEqualTo(ProblemDetail.builder().title("Boom.").status(500).instance("/boom").build());
  }
}
