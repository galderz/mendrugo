You are a GraalVM developer and you want to build SubstrateVM inside a Nix shell on a Apple Mac.

Your objective is to craft a Nix shell script that when entered into the `graal/substratevm` directory,
you can execute `mx build` and SubstrateVM builds successfully.

You can use the following paths:
* For XCode: /nix/store/dqxywksq5xmmpmrr0bybkjpszj08ysjq-Xcode26.1.1-MacOSX26
* `mx` is a Python build tool that can be checked out from https://github.com/graalvm/mx.
Note that running `mx` modifies creates directory under `mx` itself,
so storing `mx` folder to the nix store is tricky.
* `mx` requires a JAVA_HOME being set. You can set JAVA_HOME to /nix/store/b7wai98rz8iimr0qkbjwxv2y5zhjq2nc-labsjdk-ce-latest-jvmci-25.1-b10_aarch64

To build GraalVM:
1. Checkout https://github.com/graalvm/mx in the current directory
2. Checkout https://github.com/oracle/graal in the current directory
3. The Nix shell script that you craft will require `python3` package
4. In the Nix shell set `MX_PYTHON` to the `python3` binary path 
4. To do the build, `cd graal/substratevm` and execute `mx build`
