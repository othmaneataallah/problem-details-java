package com.example.demo;

import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import io.github.othmaneataallah.problemdetails.registry.ProblemType;
import io.github.othmaneataallah.problemdetails.registry.ProblemTypeRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Shared problem types and extension keys for the demo endpoints. */
@Configuration
public class DemoProblems {

  static final ProblemDetailKey<Integer> BALANCE = ProblemDetailKey.of("balance", Integer.class);

  static final String OUT_OF_CREDIT = "https://example.com/probs/out-of-credit";
  static final String VALIDATION_ERROR = "https://example.net/validation-error";

  /** Registry modelling an API that reuses documented problem types. */
  @Bean
  ProblemTypeRegistry problemTypeRegistry() {
    ProblemTypeRegistry registry = new ProblemTypeRegistry();
    registry.register(
        ProblemType.of(
            OUT_OF_CREDIT,
            "You do not have enough credit.",
            403,
            "https://example.com/docs/probs"));
    registry.register(
        ProblemType.of(
            VALIDATION_ERROR, "Your request is not valid.", 422, "https://example.net/docs"));
    return registry;
  }
}
