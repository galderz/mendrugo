# Subtype in Record

## JVM mode

One shell:
```bash
$ mvn install -DskipTests && mvn quarkus:run -pl server
```

Another shell:
```bash
$ mvn quarkus:run -pl server -pl client
```

Then first shell should show:
```bash
2023-12-27 12:41:29,965 INFO  [org.acm.ser.MammalResource] (executor-thread-1) Received mammal family:
MammalFamily[mammals=[Whale[swimSpeed=30.0, color=white], Elephant[hornLength=10, continent=africa]]]
```

## Native mode

One shell:
```bash
$ mvn install -DskipTests && mvn quarkus:run -pl server
```

Another shell:
```bash
$ mvn install -DskipTests -Dnative -pl client && ./client/target/client-1.0.0-SNAPSHOT-runner
```

Then first shell should show:
```bash
```

