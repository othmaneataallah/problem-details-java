# Jersey demo

A runnable consumer of `problem-details-java` **as released on Maven Central** (version pinned in `pom.xml` via `problem-details.version`). It lives outside the library's Maven reactor on purpose: it builds and runs exactly like a user project. Grizzly serves HTTP on port 8081, so both demos can run side by side.

## Run it

```sh
cd demo/jersey
../../mvnw compile exec:java
```

Endpoints, one per case:

| Endpoint                | Case                                                        |
|-------------------------|-------------------------------------------------------------|
| `GET /demo/ok`          | Control case, no problem                                    |
| `GET /demo/credit`      | Returned problem, negotiates `problem+json` / `problem+xml` |
| `GET /demo/fail`        | Thrown problem → mapper answers JSON even for XML Accept    |
| `GET /demo/boom`        | No status member → 500 with a faithful body                 |
| `GET /demo/validate`    | Validation-style `errors` extension, negotiable             |
| `GET /demo/types`       | The registry itself, as JSON                                |

## Verify it

With the app running, in another shell:

```sh
./verify.sh
```

The script curls every endpoint (both media types where negotiable) and asserts HTTP status, content type, and key body fields. It exits non-zero on the first mismatch.
