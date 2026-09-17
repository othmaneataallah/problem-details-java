# Compatibility

Minimums that are tested, not guessed — CI builds and tests every change on both JDK 17 and JDK 25.

| Concern          | Version                                                        |
|------------------|----------------------------------------------------------------|
| Your runtime     | Java 17+                                                       |
| Build JDK        | 25 (artifacts target Java 17)                                  |
| JSON             | Jackson 3 (`tools.jackson.core:jackson-databind`)              |
| XML              | JDK only — no third-party dependency                           |
| Spring module    | Spring Framework 7 (`spring-web`); MVC and WebFlux both covered |
| JAX-RS module    | Jakarta REST 4 API (`provided` scope)                          |
| Tests            | JUnit 6 + AssertJ                                              |

Notes and honest limits:

- **Jackson 2 is not supported.** Jackson 3 is the current major line and Spring Boot 4's default; Jackson 2 support is deprecated upstream. The JSON module is Jackson 3 only.
- **Spring 6 / Boot 3 are untested.** The Spring module builds against Spring 7, whose baseline is still Java 17 — but Boot 3's line is end-of-life, so upgrade rather than backport.
- **Jakarta REST 3.x runtimes** will likely run the JAX-RS module (the APIs used are unchanged), but only Jakarta 4 is tested.
- Snapshots (`0.2.0-SNAPSHOT`) are never published; releases are cut as version tags and staged on Maven Central.
