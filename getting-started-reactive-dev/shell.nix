{ pkgs ? import <nixpkgs> {} }:

let
  devkit = "/nix/store/vhsix1jn849mpxggwbw2zh1nbxpy0grc-Xcode16.2-MacOSX15";
in
pkgs.mkShell {
  packages = [
    pkgs.gnumake
    pkgs.python3
    # Does not work to avoid error:
    # ld: library not found for -lz
    # pkgs.zlib

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
  '' ;
}
