# Troubleshooting Native Image Builds

## Object In Image Heap Error

**Error pattern:**

```
> com.oracle.graal.pointsto.util.ParallelExecutionException: An object of type 'xyz' was found in the image heap. This type, however, is marked for initialization at image run time for the following reason: classes are initialized at run time by default.
This is not allowed for correctness reasons: All objects that are stored in the image heap must be initialized at build time.

You now have two options to resolve this:

1) If it is intended that objects of type 'xyz' are persisted in the image heap, add

    '--initialize-at-build-time=xyz'

to the native-image arguments. Note that initializing new types can store additional objects to the heap. It is advised to check the static fields of 'io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeDirectByteBuf' to see if they are safe for build-time initialization,  and that they do not contain any sensitive data that should not become part of the image.

2) If these objects should not be stored in the image heap, you can use

    '--trace-object-instantiation=xyz'

to find classes that instantiate these objects. Once you found such a class, you can mark it explicitly for run time initialization with

    '--initialize-at-run-time=<culprit>'

to prevent the instantiation of the object.
```

**Diagnose:**

1. Ignore the first suggestion and follow instructions in second suggestion.
In this example, addd `--trace-object-instantiation=xyz` to the native-image invocation and re run the command.
If the command is invoked in a script and `xyz` contains the `$` sign, make sure you escape it with `\`.

2. Once you run with with `--trace-object-instantiation=xyz` might look like this, in which case you should be able to fix the issue by adding `--initialize-at-run-time=abc`

```
The culprit object has been instantiated by the 'abc' class initializer with the following trace:
    ...
	at abc.<clinit>(abc.java:40)
```

3. Re-run with `--initialize-at-run-time=abc` and this particular issue should be fixed.
