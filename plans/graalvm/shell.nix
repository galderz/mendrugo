{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    python3
    git
    gcc
    clang
    ninja
    zlib
    zlib.static
  ];

  shellHook = ''
    # Ensure directories are created with write permissions
    umask 0022

    # Set JAVA_HOME to the labsjdk path
    export JAVA_HOME=/nix/store/b7wai98rz8iimr0qkbjwxv2y5zhjq2nc-labsjdk-ce-latest-jvmci-25.1-b10_aarch64/Contents/Home

    # Set MX_PYTHON to python3 binary
    export MX_PYTHON=${pkgs.python3}/bin/python3

    # Bypass JVMCI version check (JDK has b10 but Graal requires b13)
    export JVMCI_VERSION_CHECK=warn

    # Note: XCODE_DIR and SDKROOT are NOT needed - Nix clang wrapper handles SDK paths automatically

    # Create wrappers that redirect to clang
    mkdir -p /tmp/graalvm-wrappers

    # xcrun wrapper
    cat > /tmp/graalvm-wrappers/xcrun << 'XCRUNEOF'
#!/bin/bash
exec "$@"
XCRUNEOF
    chmod +x /tmp/graalvm-wrappers/xcrun

    # gcc wrapper - redirect to clang
    cat > /tmp/graalvm-wrappers/gcc << 'GCCEOF'
#!/bin/bash
exec clang "$@"
GCCEOF
    chmod +x /tmp/graalvm-wrappers/gcc

    # g++ wrapper - redirect to clang++
    cat > /tmp/graalvm-wrappers/g++ << 'GXXEOF'
#!/bin/bash
exec clang++ "$@"
GXXEOF
    chmod +x /tmp/graalvm-wrappers/g++

    # Add wrappers to PATH first (to override system tools)
    export PATH="/tmp/graalvm-wrappers:$PATH"

    # Add mx to PATH if it exists in current directory
    if [ -d "$PWD/mx" ]; then
      export PATH="$PWD/mx:$PATH"
    fi

    # Add JAVA_HOME/bin to PATH
    export PATH="$JAVA_HOME/bin:$PATH"

    # Use clang for C++ compilation (g++ doesn't support -ObjC++ flag)
    export CXX=clang++
    export CC=clang

    # Add zlib library path for native-image linking
    export NIX_LDFLAGS="-L${pkgs.zlib}/lib $NIX_LDFLAGS"
    export LIBRARY_PATH="${pkgs.zlib}/lib:$LIBRARY_PATH"

    echo "GraalVM SubstrateVM Build Environment"
    echo "======================================"
    echo "JAVA_HOME: $JAVA_HOME"
    echo "MX_PYTHON: $MX_PYTHON"
    echo ""
    echo "To build SubstrateVM:"
    echo "1. Clone mx: git clone https://github.com/graalvm/mx.git (if not already done)"
    echo "2. Clone graal: git clone https://github.com/oracle/graal.git (if not already done)"
    echo "3. cd graal/substratevm"
    echo "4. mx build"
  '';
}
