# FAQ

## Which pieces do I actually need?

`problem-details-core`, always. Then add by output: the Jackson module for JSON, the XML module for XML, one framework module if you run Spring or Jakarta REST, the registry if you want reusable error types. Nothing drags in anything you didn't ask for.

## Why did my XML number come back as text?

Because in XML it *is* text: `<balance>30</balance>` carries no type information, so it reads back as `"30"`. That's how the RFC's XML format works, and nothing is guessed on the way back — which is also why values like `"01234"` survive intact. Need typed round-trips? Use JSON.

## Why 500 when my error has no status?

An HTTP error response needs a status code, and yours didn't name one — so both framework integrations answer 500. If that's misleading, set an explicit `status`. (Small difference between them: Jakarta keeps your body exactly as built; Spring's own object records the 500.)

## I never set `instance`, yet responses have one. Why?

That's Spring helping: it fills a missing `instance` from the request path. Set one yourself and yours wins.

## Where does my custom data go?

In typed fields on the problem itself ([Core concepts](core.md#typed-extension-members)) — never by parsing `detail`. The detail string is for humans; machines read fields. And check the [Registry](registry.md) before inventing a whole new error type.

## Is it thread-safe?

Built errors, keys, types, and registries: yes, share them freely. Builders: no — build in one place, then share the result. A configured `JsonMapper` is thread-safe; the XML codec keeps no state at all.
