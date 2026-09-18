# Compatibility

Tested combinations — CI builds and tests every change on JDK 17 and 25, so these are facts, not hopes.

| You need              | Version                                                        |
|-----------------------|----------------------------------------------------------------|
| Java to run it        | 17 or newer                                                    |
| JSON                  | Jackson 3                                                      |
| XML                   | Nothing extra (JDK only)                                       |
| Spring module         | Spring Framework 7, MVC or WebFlux                             |
| JAX-RS module         | Jakarta REST 4 (your runtime provides it)                      |
| Tests (contributors)  | JUnit 6 + AssertJ                                              |

A few straight answers:

- **Jackson 2? No.** Jackson 3 is the current major line and what Spring Boot 4 ships. Jackson 2 support is deprecated upstream — staying on it would date the library on arrival.
- **Spring Boot 3? Untested, and its line is end-of-life.** The Spring module builds against Spring 7, which still runs on Java 17, so upgrading Boot is the only sane path.
- **Jakarta REST 3.x?** Probably works (the APIs used haven't changed), but only version 4 is tested.
- **Snapshots?** Never published. Releases come as version tags on Maven Central.
