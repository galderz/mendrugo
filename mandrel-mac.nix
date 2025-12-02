{ pkgs ? import <nixpkgs> {} }:

let
  devkit = "/nix/store/dqxywksq5xmmpmrr0bybkjpszj08ysjq-Xcode26.1.1-MacOSX26";
in
pkgs.mkShell {
  packages = [
    pkgs.gnumake
    pkgs.python3
    devkit
  ];

  shellHook = ''
    export MX_PYTHON="${pkgs.python3}/bin/python3"
    echo "Setting MX_PYTHON to $MX_PYTHON"

    echo "Set gcc patch to dev kit"
    export PATH=${devkit}/Xcode/Contents/Developer/usr/bin:$PATH

    # Does not work:
    # echo "Set cc patch to dev kit"
    # export PATH=${devkit}/Xcode/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin:$PATH

    # export NATIVE_IMAGE_OPTIONS="-H:-CheckToolchain"
    # echo "Setting NATIVE_IMAGE_OPTIONS to $NATIVE_IMAGE_OPTIONS"
  '' ;
}
