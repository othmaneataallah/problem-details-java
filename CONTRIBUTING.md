# Contributing

Thanks for your interest in `problem-details-java`. Small, focused contributions beat big ones — a clear bug report is already a contribution.

## How to help

- **Use it and report back.** Real-world usage finds what tests miss: confusing APIs, missing docs, surprising behavior.
- **Report bugs** with a minimal reproduction (input, expected output, actual output, Java and dependency versions).
- **Suggest features** by describing the problem first; API proposals come second.
- **Fix things** via pull request (see below).

## Working on the code

Prerequisites: JDK 25 (pinned in `.sdkmanrc`) and the Maven wrapper — no other toolchain needed.

```sh
./mvnw clean verify   # full build, tests, format check
./mvnw spotless:apply # fix formatting (Google Java Style, enforced in CI)
```

Conventions that matter:

- Java 17 is the compatibility floor: no newer language features or APIs in main code.
- `problem-details-core` stays dependency-free — framework or serialization imports there are a design discussion first, never a drive-by.
- Public API needs Javadoc, and behavior changes need tests. The bar is in `AGENTS.md`.
- One logical change per pull request, with tests and docs updated alongside.

## Pull requests

1. Fork, branch from `main` (`feat/...`, `fix/...`, `docs/...`, `chore/...`).
2. Keep the change tight; explain non-obvious design calls in the PR body.
3. Make sure `./mvnw clean verify` and the format check pass — CI runs both on JDK 17 and 25.
4. Maintainers merge with squash once CI is green and review is done.

## Releases

Only maintainers cut releases; the process is documented in `AGENTS.md`. If you need something released, open an issue and say so.
