# Benchmarking Native GC policies

One shell:
```bash
make run-s
```

Second shell:
```bash
make jvisualvm
```

Connect jvisualvm to the Quarkus application.

Third shell:
```bash
make hyperfoil
[hyperfoil@in-vm]$ start-local
[hyperfoil@in-vm]$ wrk -t 128 -c 512 -H 'accept: text/plain' -d 16m http://<host>:8080/hello
```

Sample results for space/time:
```bash
PHASE        METRIC   THROUGHPUT    REQUESTS  MEAN     p50      p90      p99      p99.9     p99.99     TIMEOUTS  ERRORS  BLOCKED   2xx       3xx  4xx  5xx  CACHE
test         request  93.79k req/s  90036541  5.46 ms  5.47 ms  6.00 ms  9.04 ms  19.66 ms   56.89 ms         0       0      0 ns  90036094    0    0    0      0
```

Sample results for adaptive:
```bash
PHASE        METRIC   THROUGHPUT    REQUESTS  MEAN     p50      p90      p99      p99.9     p99.99     TIMEOUTS  ERRORS  BLOCKED   2xx       3xx  4xx  5xx  CACHE
test         request  93.05k req/s  89329151  5.50 ms  5.54 ms  6.00 ms   9.11 ms  16.71 ms   41.68 ms         0       0     0 ns  89328711    0    0    0      0
```

