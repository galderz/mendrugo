{ pkgs ? import <nixpkgs> {} }:

let
  unstable = import (builtins.fetchTarball
    "https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz") {};

  devkit = "/nix/store/dqxywksq5xmmpmrr0bybkjpszj08ysjq-Xcode26.1.1-MacOSX26";
in
pkgs.mkShell {
  packages = [
    devkit
    pkgs.gnumake
    pkgs.maven
    unstable.graalvmPackages.graalvm-ce
  ];

  GRAALVM_HOME="${unstable.graalvmPackages.graalvm-ce}";

  shellHook = ''
    echo "GRAALVM_HOME set to $GRAALVM_HOME"
  '' ;
}
