{ pkgs ? import <nixpkgs> {} }:

pkgs.stdenv.mkDerivation rec {
  pname = "mx";
  version = "latest";

  src = pkgs.fetchFromGitHub {
    owner = "graalvm";
    repo = "mx";
    rev = "b2bfb075e745dd81140e8f2b6923c91a8d0b1a51";
    # obtained with: $ nix-prefetch-url --unpack https://github.com/graalvm/mx/archive/b2bfb075e745dd81140e8f2b6923c91a8d0b1a51.tar.gz
    sha256 = "sha256-0snjxdv9v354pmkywsmf3sb4040747f3bmas9s96y8vg49hrqj1h";
  };

  buildInputs = [
    pkgs.python3
  ];

  installPhase = ''
    mkdir -p $out/bin
    cp -r $src $out/mx
    ln -s $out/mx/mx $out/bin/mx
  '';

  meta = {
    description = "A small framework to manage the GraalVM compiler and language implementations.";
    license = pkgs.lib.licenses.gpl2;
    homepage = "https://github.com/graalvm/mx";
  };
}
