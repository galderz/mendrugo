# Benchmarking Quarkus Plain Text

One shell:
```bash
make
```

Second shell:
```bash
make hyperfoil
[hyperfoil@in-vm]$ start-local
[hyperfoil@in-vm]$ wrk -t 128 -c 512 -H 'accept: text/plain' -d 16m http://<host>:8080/hello
```
