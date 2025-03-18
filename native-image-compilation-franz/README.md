# A Nix experiment for GraalVM/Mandrel

Rather than adding all the mx workings inside the shell.nix file,
it would be better to have that defined inside the mx default.nix,
and then add what is needed (e.g. a boot JDK that works) to the shell.nix file.
Also note that you will likely need some kind of temurin snapshot with static libraries.

```shell
> nix-shell
$ make idea
cd mandrel/substratevm
mx intellijinit
Could not find a JDK
Specify one with the --java-home or --extra-java-homes option or with the JAVA_HOME or EXTRA_JAVA_HOMES environment variable.
Or run `/private/tmp/mx-writable/select_jdk.py -p /Users/galder/1/mandrel-native-image-compilation-franz/substratevm` to set and persist these variables in /Users/galder/1/mandrel-native-image-compilation-franz/substratevm/mx.substratevm/env.
make: *** [Makefile:13: idea] Error 1
```
