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
