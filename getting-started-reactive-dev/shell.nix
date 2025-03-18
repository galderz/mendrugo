{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [
    pkgs.gnumake
    pkgs.python3
  ];

  shellHook = ''
    export MX_PYTHON="${pkgs.python3}/bin/python3"
    echo "Setting MX_PYTHON to $MX_PYTHON"
  '' ;
}
