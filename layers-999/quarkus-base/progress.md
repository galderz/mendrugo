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
