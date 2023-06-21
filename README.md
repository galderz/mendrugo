# Podman and macOS

[Issue](https://github.com/quarkusio/quarkus/issues/33188)

Run `quarkus-dev-plaintext` with 
`MAVEN_ARGS=-Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman"`.

# Building with native reports

E.g.

```bash
cd quarkus-quick-crud
make build MAVEN_ARGS="-Dquarkus.native.enable-reports"
```
