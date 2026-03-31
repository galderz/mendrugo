# Building Quarkus

The Quarkus source code is available as an additional directory at `~/src/quarkus-layers`.

## Full build

```bash
make quarkus
```

This builds core and all tracked extensions (vertx, grpc, hibernate-orm, etc.) via `Quarkus.gmk`. It uses `./mvnw -DskipTests` for each module and installs them locally.

The Make variable `QUARKUS_HOME` controls the source location (defaults to `~/src/quarkus-$(ID)` where `ID` defaults to `layers`).

## Build a single extension

```bash
make quarkus_extensions_vertx_jar
```

Replace `vertx` with the extension name. Available targets follow the pattern `quarkus_extensions_<name>_jar`. See `Quarkus.gmk` for the full list.

## Quick build (upstream shortcut)

From the Quarkus source directory directly:

```bash
cd ~/src/quarkus-layers && ./mvnw -Dquickly
```

This is what `make build-quarkus` runs.

## Testing

```bash
make test-quarkus TEST_MODULE=main
```

Optional variables: `CONTAINER=1` (enable test containers), `DEBUG=1` (use mvnDebug), `TRACE=1` (enable trace logging).
