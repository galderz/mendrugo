# Progress

## 2026-04-13

### Error: `build-layer-base.sh` fails with `NoClassDefFoundError`

```
Error: Cannot find io.netty.handler.ssl.SslContext.getPrivateKeyFromByteBuffer,
io.netty.handler.ssl.SslContext can not be loaded, due to
io/netty/channel/ChannelHandlerAdapter not being available in the classpath.
```

### Analysis

Added diagnostic logging to Mandrel's `AnnotationSubstitutionProcessor.findOriginalMethod`
(catch `LinkageError` block) and class load tracing to `NativeImageClassLoader.findClassViaClassPath`.

**Q1: Why does it try to load `SslContext.getPrivateKeyFromByteBuffer`?**

The Quarkus substitution class `Target_SslContext` (in `NettySubstitutions.java:574`) has an
`@Alias` annotation on `getPrivateKeyFromByteBuffer`. During substitution processing
(`AnnotationSubstitutionProcessor.init` -> `handleAliasClass` -> `handleMethodInAliasClass`
-> `findOriginalMethod`), Mandrel calls `SslContext.class.getDeclaredMethod("getPrivateKeyFromByteBuffer", ...)`
to resolve the alias target. This triggers the JVM's `getDeclaredMethods0` native method,
which resolves all method signatures on `SslContext`, including ones that reference types
extending `ChannelHandlerAdapter`.

**Q2: How does `SslContext` depend on `ChannelHandlerAdapter`?**

Class load trace shows the dependency chain:

```
getDeclaredMethods0(SslContext)
  -> resolves method signature referencing SslHandler
    -> SslHandler extends ByteToMessageDecoder
      -> ByteToMessageDecoder extends ChannelInboundHandlerAdapter
        -> ChannelInboundHandlerAdapter extends ChannelHandlerAdapter (MISSING)
```

`ChannelHandlerAdapter` lives in the `io.netty.channel` transport JAR which is not on the
classpath for this layer build.

### Workaround attempt: skip `Target_SslContext` during base layer build

Quarkus commit: `fc7ec65e3f7` ("Attempt to only load substitution when building base layer")

Added `IsAppLayerBuild` `BooleanSupplier` that checks `-Dquarkus.native.base-layer-build=true`
and used it in `@TargetClass(onlyWith = {IsBouncyNotThere.class, IsAppLayerBuild.class})` on
`Target_SslContext`. Also passed `-J-Dquarkus.native.base-layer-build=true` in
`build-layer-base.sh`.

This fixed the `AnnotationSubstitutionProcessor` error, but the build still fails with a bare
`NoClassDefFoundError: io/netty/channel/ChannelHandlerAdapter` later during image generation.
233 classes transitively fail to load because they depend on `ChannelHandlerAdapter` through
their class hierarchy. Examples:

```
io.netty.channel.ChannelInboundHandlerAdapter
io.netty.channel.ChannelOutboundHandlerAdapter
io.netty.channel.ChannelDuplexHandler
io.netty.handler.codec.ByteToMessageDecoder
io.netty.handler.codec.MessageToMessageDecoder
io.netty.handler.codec.http.HttpObjectDecoder
io.netty.handler.codec.http2.Http2ConnectionHandler
io.netty.handler.ssl.SslHandler
io.vertx.core.http.impl.VertxHttp2ConnectionHandler
io.vertx.core.net.impl.VertxHandler
```

The problem is fundamental: `ChannelHandlerAdapter` is the base class for virtually all Netty
channel handlers, so excluding the `io.netty.channel` transport JAR from the classpath breaks
most of the Netty handler ecosystem. A different approach is needed.

### Next error: `NoClassDefFoundError: io/vertx/core/impl/ContextInternal`

After the `ChannelHandlerAdapter` issue was resolved, the build fails with a new
`NoClassDefFoundError` for `ContextInternal`:

```
java.lang.NoClassDefFoundError: io/vertx/core/impl/ContextInternal
  at java.lang.Class.getDeclaredMethods0(Native Method)
  at ...AnnotationSubstitutionProcessor.handleAliasClass(:452)
```

Same pattern, different failure point. The substitution class
`Target_io_vertx_core_eventbus_impl_clustered_ClusteredEventBusClusteredEventBus`
in `VertxSubstitutions.java:73` has a `@Substitute` method `createHandlerHolder` (line 103)
with `ContextInternal` as a parameter type. When `handleAliasClass` calls
`annotatedClass.getDeclaredMethods()` to enumerate the substitution class's own methods,
the JVM resolves all method signatures and fails because `ContextInternal` is missing from the
modified `vertx-core` JAR (the JAR contains `ContextImpl`, `ContextBase`, etc. but not
`ContextInternal`).

This is the same root problem as `ChannelHandlerAdapter`: the stripped classpath for the layer
build is missing classes that substitution classes reference in their method signatures. Fixing
these one-by-one with `onlyWith` guards is not scalable — the approach of selectively stripping
JARs for the layer build conflicts with Mandrel's substitution processing which needs to resolve
all referenced types.

### `PlatformDependent` build-time init fails due to missing `CleanerJava9`

With the substitution errors resolved, the build progressed further but
`io.netty.util.internal.PlatformDependent` failed to initialize at build time.

Added Mandrel logging to `ClassInitializationSupport.computeInitKindAndMaybeInitializeClass`
(reason, superclass, and stack trace for `NioServerSocketChannel`) which confirmed:

- `NioServerSocketChannel` is registered for reflection by `ReflectionDataBuilder.registerClass`,
  making it reachable, which triggers `SVMHost.onTypeReachable` → `maybeInitializeAtBuildTime`.
- `specifiedInitKind=BUILD_TIME` because `META-INF/native-image/io.netty/netty-codec-http/native-image.properties`
  and `netty-codec-http2/native-image.properties` both specify `--initialize-at-build-time=io.netty`.
- `NioServerSocketChannel.<clinit>` → `SelectorProviderUtil.findOpenMethod` →
  `PlatformDependent.<clinit>` → `NoClassDefFoundError: io/netty/util/internal/CleanerJava9`
  (stripped from modified netty-common JAR).
- `PlatformDependent` gets silently demoted from BUILD_TIME to RUN_TIME, causing cascading
  failures in all classes that depend on it during their static initializers.

**Workaround:** Added `--initialize-at-run-time=io.netty.util.internal.PlatformDependent` to
`build-layer-base.sh` to prevent the cascading build-time initialization failure.

### `ProcessHandleImpl` object in image heap

Build failed with `ProcessHandleImpl` found in image heap, reached via static field
`io.smallrye.common.os.Process.current`. Used `--trace-object-instantiation=java.lang.ProcessHandleImpl`
(required building the diagnostics agent first with
`mx native-image -g --macro:native-image-diagnostics-agent-library`).
The trace confirmed `io.smallrye.common.os.Process` as the culprit class — it holds both
`ProcessHandleImpl` (field `current`) and `ProcessHandleImpl$Info` (field `currentInfo`).

**Workaround:** Added `--initialize-at-run-time=io.smallrye.common.os.Process` to
`build-layer-base.sh`.

### Base layer build succeeds

With all workarounds in place, `build-layer-base.sh` completes successfully:
- `target/libquarkusbaselayer.so` (164.63MiB)
- `target/libquarkusbaselayer.nil` (1.15GiB)
- Build time: 1m 23s

### Mandrel logging changes

Protected the diagnostic logging added earlier behind flags:
- `AnnotationSubstitutionProcessor`: gated behind `-J-Dsvm.traceLinkageErrors=true`
- `NativeImageClassLoader` class load tracing: gated behind `-J-Dsvm.traceClassLoad=true`
