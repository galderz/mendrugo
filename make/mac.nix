{ pkgs ? import <nixpkgs> {} }:

let
  # Fetch mx build tool from GitHub
  mxSrc = pkgs.fetchFromGitHub {
    owner = "graalvm";
    repo = "mx";
    rev = "master";
    hash = "sha256-UFyPzv3vXi6B+R6Nm2oAn46K/RQbpBq9VEE8LBh7eL4=";
  };
in
pkgs.mkShell {
  buildInputs = with pkgs; [
    python3
    git
    gcc
    clang
    ninja
    zlib
    zlib.static
    libcxx
  ];

  shellHook = ''
    # Ensure directories are created with write permissions
    umask 0022

    # Set JAVA_HOME to the labsjdk path
    export JAVA_HOME=/nix/store/idw58k6a6jw8jlgad5463rf9apwzamn4-labsjdk-ce-latest-jvmci-25.1-b14_aarch64/Contents/Home

    # Set MX_PYTHON to python3 binary
    export MX_PYTHON=${pkgs.python3}/bin/python3

    # Bypass JVMCI version check (JDK has b10 but Graal requires b13)
    export JVMCI_VERSION_CHECK=warn

    # Note: XCODE_DIR and SDKROOT are NOT needed - Nix clang wrapper handles SDK paths automatically

    # Setup mx in a writable location (mx modifies its own directory during execution)
    # Use ~/Library/Caches for macOS standard cache location (won't be cleaned during runtime)
    export MX_HOME="$HOME/Library/Caches/graalvm-mx"
    if [ ! -d "$MX_HOME" ]; then
      echo "Setting up mx build tool in $MX_HOME..."
      mkdir -p "$MX_HOME"
      cp -r ${mxSrc}/* "$MX_HOME/"
      chmod -R u+w "$MX_HOME"

      # Patch mx_util.py to add write permissions when creating directories from Nix store
      echo "Applying mx_util.py patch for Nix compatibility..."
      sed -i.bak 's/os.makedirs(path, mode=mode)/os.makedirs(path, mode=mode | 0o200)/' "$MX_HOME/src/mx/_impl/mx_util.py"
    fi

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

    # Add wrappers and mx to PATH
    export PATH="/tmp/graalvm-wrappers:$MX_HOME:$PATH"

    # Add JAVA_HOME/bin to PATH
    export PATH="$JAVA_HOME/bin:$PATH"

    # Use clang for C++ compilation (g++ doesn't support -ObjC++ flag)
    export CXX=clang++
    export CC=clang

    # Add zlib library path for native-image linking
    export NIX_LDFLAGS="-L${pkgs.zlib}/lib $NIX_LDFLAGS"
    export LIBRARY_PATH="${pkgs.zlib}/lib:$LIBRARY_PATH"

    # Add C++ standard library path for LLVM toolchain linking
    export LIBRARY_PATH="${pkgs.libcxx}/lib:$LIBRARY_PATH"

    echo "GraalVM SubstrateVM Build Environment"
    echo "======================================"
    echo "JAVA_HOME: $JAVA_HOME"
    echo "MX_PYTHON: $MX_PYTHON"
    echo "MX_HOME: $MX_HOME"
    echo ""
    echo "To build SubstrateVM:"
    echo "1. Clone graal: git clone https://github.com/oracle/graal.git (if not already done)"
    echo "2. cd graal/substratevm"
    echo "3. mx build"
  '';
}
