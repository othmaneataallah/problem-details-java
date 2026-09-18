# Demos

Two small apps that run the released library end to end — the fastest way to see it behave, and to check your own setup against.

Both live in [`demo/`](https://github.com/othmaneataallah/problem-details-java/tree/main/demo), build independently of the library, and ship a `verify.sh` script that starts nothing and assumes nothing: it waits for the app, curls every endpoint, and checks status codes, content types, and body fields.

## Spring Boot demo

[`demo/spring-boot`](https://github.com/othmaneataallah/problem-details-java/tree/main/demo/spring-boot) — a Spring Boot app showing the everyday cases: a full error with custom data, an error whose location fills in automatically, the 500 fallback, a validation-style error list, and the registry served as JSON.

```sh
cd demo/spring-boot
../../mvnw spring-boot:run   # in one shell
./verify.sh                  # in another
```

## Jersey demo

[`demo/jersey`](https://github.com/othmaneataallah/problem-details-java/tree/main/demo/jersey) — a Jersey/Grizzly app on port 8081 (so both demos can run together). Same idea, plus the one thing only it can show: the same error served as JSON *or* XML depending on the `Accept` header.
