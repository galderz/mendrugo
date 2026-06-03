# ClassCastException in CrossLayerConstantRegistryFeature.isConstantRegistered()

## Summary

Building a native image layer with `--initialize-at-build-time=""` crashes during
the image creation phase (step 8/8) with a `ClassCastException`:

```
java.lang.ClassCastException: class java.lang.Package cannot be cast to class
com.sun.org.apache.xml.internal.serializer.CharInfo$CharKey
```

## Root Cause

`CrossLayerConstantRegistryFeature.isConstantRegistered(Object obj)` uses
`ConcurrentHashMap.containsValue(obj)`, which iterates over all map values and
calls `value.equals(obj)` for each one.

The `constantCandidates` map contains objects of mixed types. When a
`CharInfo$CharKey` value is compared against a non-`CharKey` object (e.g.,
`java.lang.Package`), the `CharKey.equals()` method throws `ClassCastException`
because it performs an unchecked cast:

```java
// CharInfo$CharKey.equals() — java.xml module
public final boolean equals(Object obj) {
    return ((CharKey) obj).m_char == m_char;  // No instanceof check!
}
```

## Call Stack

```
CharInfo$CharKey.equals(CharInfo.java:742)
ConcurrentHashMap.containsValue(ConcurrentHashMap.java:997)
CrossLayerConstantRegistryFeature.isConstantRegistered(CrossLayerConstantRegistryFeature.java:356)
SVMImageConstantSnapshotWriter.shouldRelinkConstant(SVMImageConstantSnapshotWriter.java:239)
SVMImageConstantSnapshotWriter.persistConstantRelinkingInfo(SVMImageConstantSnapshotWriter.java:222)
SVMImageConstantSnapshotWriter.persistConstant(SVMImageConstantSnapshotWriter.java:169)
SVMImageConstantSnapshotWriter.writeConstants(SVMImageConstantSnapshotWriter.java:137)
SVMImageLayerWriter.persistAnalysisInfo(SVMImageLayerWriter.java:244)
NativeImageGenerator.doRun(NativeImageGenerator.java:780)
```

## Reproducer

### Prerequisites

- GraalVM CE or Mandrel with native image layer support (`-H:LayerCreate`)
- Linux x86_64

### Steps

```bash
export JAVA_HOME=/path/to/graalvm-or-mandrel
./build-base-layer.sh
```

### Expected

The base layer build should complete successfully.

### Actual

The build fails during image creation (step 8/8) with:

```
java.lang.ClassCastException: class java.lang.Package cannot be cast to class
com.sun.org.apache.xml.internal.serializer.CharInfo$CharKey
(java.lang.Package is in module java.base of loader 'bootstrap';
com.sun.org.apache.xml.internal.serializer.CharInfo$CharKey is in module java.xml
of loader 'bootstrap')
```

### Why it happens

The build uses `--initialize-at-build-time=""` which initializes all classes at
build time, including `com.sun.org.apache.xml.internal.serializer.CharInfo`. The
`CharInfo` class populates a `HashMap<CharKey, String>` during its static
initializer. These `CharKey` objects end up in the image heap as constants.

During the image creation phase, the layer writer iterates over all constants
and calls `isConstantRegistered()` for each one. This method calls
`ConcurrentHashMap.containsValue(obj)` which iterates over ALL registered
constant candidates and calls `equals()` on each value. When a `CharKey`
value's `equals()` is called with a `Package` object, the unchecked cast fails.

## Proposed Fix

Replace `ConcurrentHashMap.containsValue(obj)` with an identity-based lookup
in `CrossLayerConstantRegistryFeature.isConstantRegistered()`:

```java
public boolean isConstantRegistered(Object obj) {
    for (Object value : constantCandidates.values()) {
        if (value == obj) {
            return true;
        }
    }
    return false;
}
```

This is correct because constant registration uses object identity, not
equality. The `registerConstantCandidate` method uses `putIfAbsent` with a
String key, and the value is expected to be a specific object instance, not
just an equal one.

## Notes

- The bug is in `CharKey.equals()` which violates the `equals()` contract by
  not checking `instanceof` before casting. However, `CharKey` is a JDK
  internal class that wasn't designed for use in heterogeneous collections.
- The GraalVM layer infrastructure should not rely on `equals()` for identity
  checks on arbitrary heap objects, since any class with a broken `equals()`
  implementation could trigger this bug.
- This bug can be triggered by any class with a broken `equals()` that happens
  to be in the image heap during a layered build with build-time initialization.
