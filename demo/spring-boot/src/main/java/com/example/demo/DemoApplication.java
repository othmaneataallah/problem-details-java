package com.example.demo;

import io.github.othmaneataallah.problemdetails.spring.ProblemDetailAdvice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/** Demo entry point. The advice is registered explicitly via {@code Import}. */
@SpringBootApplication
@Import(ProblemDetailAdvice.class)
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
