#!/usr/bin/env bash
set -eux

JAVA_HOME="${JAVA_HOME:?Set JAVA_HOME to a GraalVM/Mandrel installation}"
native_image="$JAVA_HOME/bin/native-image"

# Compile the app
mkdir -p classes target
"$JAVA_HOME/bin/javac" --limit-modules java.base,java.xml -d classes src/App.java

# Build the base layer.
#
# --initialize-at-build-time="" triggers build-time initialization of all
# classes including com.sun.org.apache.xml.internal.serializer.CharInfo
# which populates a HashMap with CharKey objects that have a broken equals().
#
# During image creation (step 8/8), the layer writer calls
# CrossLayerConstantRegistryFeature.isConstantRegistered(obj) which uses
# ConcurrentHashMap.containsValue(obj). This iterates ALL values, calling
# value.equals(obj). When a CharKey value is compared to a non-CharKey obj
# (e.g., java.lang.Package), CharKey.equals() throws ClassCastException.

"${native_image}" \
    -J-Djava.awt.headless=true \
    --initialize-at-build-time="" \
    --initialize-at-run-time=java.rmi \
    --initialize-at-run-time=sun.rmi \
    --initialize-at-run-time=jdk.tools.jlink.internal.plugins \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch \
    --initialize-at-run-time=sun.awt \
    --initialize-at-run-time=java.awt \
    --initialize-at-run-time=javax.swing \
    --initialize-at-run-time=sun.java2d \
    --initialize-at-run-time=sun.font \
    --initialize-at-run-time=java.awt.color \
    --initialize-at-run-time=java.awt.font \
    --initialize-at-run-time=java.awt.image \
    --initialize-at-run-time=javax.imageio \
    --initialize-at-run-time=com.sun.imageio \
    --initialize-at-run-time=sun.print \
    --initialize-at-run-time=com.sun.media.sound \
    --initialize-at-run-time=javax.sound \
    -H:LayerCreate=base.nil,module=java.base \
    -cp classes \
    -o libbase \
    -H:Path=target
