package com.example.demo;

import static com.example.demo.DemoProblems.BALANCE;
import static com.example.demo.DemoProblems.OUT_OF_CREDIT;
import static com.example.demo.DemoProblems.VALIDATION_ERROR;

import io.github.othmaneataallah.problemdetails.core.ProblemDetail;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailException;
import io.github.othmaneataallah.problemdetails.core.ProblemDetailKey;
import io.github.othmaneataallah.problemdetails.registry.ProblemType;
import io.github.othmaneataallah.problemdetails.registry.ProblemTypeRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** One endpoint per case, so every behavior is curl-verifiable (see verify.sh). */
@RestController
@RequestMapping("/demo")
public class DemoController {

  private final ProblemTypeRegistry registry;

  public DemoController(ProblemTypeRegistry registry) {
    this.registry = registry;
  }

  /** Control case: no problem involved. */
  @GetMapping("/ok")
  public Map<String, String> ok() {
    return Map.of("message", "ok");
  }

  /** Full problem with a typed extension, built from the registry entry. */
  @GetMapping("/credit")
  public String credit() {
    ProblemType type = registry.lookup(OUT_OF_CREDIT).orElseThrow();
    throw new ProblemDetailException(
        ProblemDetail.builder()
            .type(type.getType())
            .title(type.getTitle())
            .status(type.getRecommendedStatus())
            .detail("Your current balance is 30, but that costs 50.")
            .instance("https://example.com/instances/1")
            .extension(BALANCE, 30)
            .build());
  }

  /** No explicit instance: Spring defaults it from the request path. */
  @GetMapping("/missing")
  public String missing() {
    throw new ProblemDetailException(
        ProblemDetail.builder()
            .title("No such account.")
            .status(404)
            .detail("Account 12345 does not exist.")
            .build());
  }

  /** No status member: the integrations answer 500. */
  @GetMapping("/boom")
  public String boom() {
    throw new ProblemDetailException(ProblemDetail.builder().title("Boom.").build());
  }

  /** Validation-style extension: a list of per-field errors. */
  @GetMapping("/validate")
  public Map<String, String> validate(@RequestParam(defaultValue = "30") int age) {
    if (age >= 0) {
      return Map.of("message", "valid");
    }
    ProblemType type = registry.lookup(VALIDATION_ERROR).orElseThrow();
    Map<String, Object> error = new LinkedHashMap<>();
    error.put("detail", "must be a positive integer");
    error.put("pointer", "#/age");
    throw new ProblemDetailException(
        ProblemDetail.builder()
            .type(type.getType())
            .title(type.getTitle())
            .status(type.getRecommendedStatus())
            .extension(ProblemDetailKey.of("errors", Object.class), List.of(error))
            .build());
  }

  /** Exposes the registry itself, ordered by type URI. */
  @GetMapping("/types")
  public List<Map<String, Object>> types() {
    return registry.registeredTypes().stream()
        .map(
            type -> {
              Map<String, Object> view = new LinkedHashMap<>();
              view.put("type", type.getType().toString());
              view.put("title", type.getTitle());
              if (type.getRecommendedStatus() != null) {
                view.put("status", type.getRecommendedStatus());
              }
              return view;
            })
        .toList();
  }
}
