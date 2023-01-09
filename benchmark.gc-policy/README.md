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
```
