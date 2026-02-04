{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = with pkgs; [
    javaPackages.compiler.openjdk17-bootstrap
    graalvmPackages.graalvm-ce
    maven
  ];

  shellHook = ''
    export GRAALVM_HOME="${pkgs.graalvmPackages.graalvm-ce}"
    echo "Setting GRAALVM_HOME to $GRAALVM_HOME"

    export JAVA_HOME="${pkgs.javaPackages.compiler.openjdk17-bootstrap}"
    echo "Setting JAVA_HOME to $JAVA_HOME"

    export MAVEN_HOME="${pkgs.maven}"
    echo "Setting MAVEN_HOME to $MAVEN_HOME"
  '' ;
}
