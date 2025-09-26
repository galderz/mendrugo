# Spring Native Example

Verify the java code is fine:

```bash
./mvnw spring-boot:run
```

Query the endpoint:

```bash
$ curl localhost:8080/hello
Hello, GraalVM!%
```

Then do a native image build:

```bash
./mvnw -Pnative native:compile
```

Run the native image:

```bash
./target/demo
```

Query the endpoint:

```bash
$ curl localhost:8080/hello
Hello, GraalVM!%
```
