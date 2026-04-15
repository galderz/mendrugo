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

### App layer build fails: `guarantee failed` in `SVMImageLayerLoader`

Running `build-layer-app.sh` fails with:

```
com.oracle.svm.shared.util.VMError$HostedError: guarantee failed
  at SVMImageLayerLoader.initializeBaseLayerTypeBeforePublishing(SVMImageLayerLoader.java:709)
```

**Analysis:**

Added diagnostic logging to `SVMImageLayerLoader.initializeBaseLayerTypeBeforePublishing`
and `SVMImageLayerWriter` (gated behind `-J-Dsvm.traceLayerTypes=true`).

The guarantee checks that `type.isLinked() == typeData.getIsLinked()`. The failing type is
`io.vertx.core.impl.VertxImpl`:
- Base layer persisted `isLinked=false` because `VertxImpl` depends on `ContextInternal`
  which is missing from the stripped base layer classpath.
- App layer has `isLinked=true` because the full classpath includes `ContextInternal`.

8 types total are persisted as unlinked in the base layer:

| Type | Missing dependency |
|------|-------------------|
| `io.vertx.core.impl.VertxImpl` | `io.vertx.core.impl.ContextInternal` |
| `io.netty.handler.ssl.ConscryptAlpnSslEngine` | `org.conscrypt.BufferAllocator` |
| `io.netty.handler.ssl.ReferenceCountedOpenSslContext` | `io.netty.internal.tcnative.SSLPrivateKeyMethod` |
| `io.netty.util.internal.logging.Log4JLogger` | `org.apache.log4j.Priority` |
| `io.quarkus.vertx.runtime.jackson.QuarkusJacksonJsonCodec` | `com.fasterxml.jackson.databind.JsonSerializer` |
| `io.vertx.core.json.jackson.DatabindCodec` | `com.fasterxml.jackson.databind.Module` |
| `io.vertx.core.logging.Log4j2LogDelegate` | `org.apache.logging.log4j.message.Message` |
| `io.netty.handler.ssl.JettyNpnSslEngine` | `org.eclipse.jetty.npn.NextProtoNego$Provider` |

**Workaround:** Added `ContextInternal` to the stripped JAR via `split-jar.sh` so `VertxImpl`
links in the base layer, resolving the `isLinked` mismatch. The remaining 7 unlinked types
have optional dependencies (Conscrypt, tcnative, Log4j, Jackson, Jetty NPN) not on either
layer's classpath, so their `isLinked=false` is consistent across layers.

## 2026-04-14

### `ConsoleHandler$ConsoleHolder.console` null during field relinking

After the `isLinked` fix, app layer build fails with:

```
VMError$HostedError: Found NULL_CONSTANT when reading the hosted value of relinked field
ConsoleHandler$ConsoleHolder.console (PrintWriter)
```

The field holds a `PrintWriter` wrapping `System.out`/`System.err`. In the base layer it was
build-time initialized, but the hosted value is null in the app layer context because these
stream handles are process-specific.

**Workaround:** Added `--initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler$ConsoleHolder`
to `build-layer-base.sh`.

### Duplicate `java.util.logging.Level` constants in base layer

After the `ConsoleHolder` fix, app layer build fails with:

```
AnalysisError: Found unexpected snapshot value for base layer value.
Existing value: ImageHeapConstant<java.util.logging.Level, id: 48290>.
New value: ImageHeapConstant<java.util.logging.Level, id: 49738>.
```

Added relink tracing to `SVMImageLayerLoader.relinkStaticFinalFieldValues` (gated behind
`-J-Dsvm.traceLayerTypes=true`). The trace shows two base layer constants resolve to the
same hosted `Level` object at relink time:

- **id=48290**: `io.quarkus.bootstrap.logging.InitialConfigurator.MIN_LEVEL`
- **id=49738**: `java.util.logging.Level.FINE`

Both fields point to the same `Level` instance (int value 500, matching
`-Dlogging.initial-configurator.min-level=500`). In the base layer they were persisted as
separate constants. During app layer relinking, `registerBaseLayerValue` requires a 1:1
mapping between hosted objects and constants, so the second registration fails.

This is a Mandrel layer infrastructure issue — two base layer constants aliased to the same
singleton object need deduplication handling.

### Class initialization not stable between layers

App layer build fails with `Class initialization info not stable between layers` for
`Socks4ServerDecoder`, `Socks5InitialRequestDecoder`, `ReplayingDecoder`, and others.
In the base layer these classes are `Linked, buildTimeInit=false` (runtime init), but the
app layer expects them to be `FullyInitialized, buildTimeInit=true`.

Added demotion tracing to `ClassInitializationSupport` (gated behind
`-J-Dsvm.traceClassInitDemotions=true`). The trace reveals 328 classes demoted to runtime
init in the base layer, all cascading from `PlatformDependent`:

```
PlatformDependent → runtime-init (explicit flag, CleanerJava9 missing from stripped JAR)
  → Signal.<clinit> fails (depends on PlatformDependent)
    → ReplayingDecoder.<clinit> fails (depends on Signal)
      → Socks4ServerDecoder, Socks5InitialRequestDecoder, etc. (extend ReplayingDecoder)
  → AttributeKey, AsciiString, UnpooledByteBufAllocator, etc. (depend on PlatformDependent)
    → CharSequenceValueConverter, DecoderResult, ChannelOption, etc. (depend on above)
```

The root cause is `--initialize-at-run-time=io.netty.util.internal.PlatformDependent` in
`build-layer-base.sh`, which was added because `PlatformDependent.<clinit>` fails due to
missing `CleanerJava9` in the stripped netty-common JAR. This cascades to 328 classes that
transitively depend on `PlatformDependent` during their static initializers. In the app
layer, the full classpath is available, so these classes initialize at build time — causing
the mismatch.

### Class init instability workarounds in app layer

Added `--initialize-at-run-time` flags to `build-layer-app.sh` one by one for classes demoted
in the base layer (PlatformDependent cascade). The full list of classes added includes
netty codecs, handlers, vertx HTTP/impl classes, channel options, buffer pools, etc.
After removing `--link-at-build-time` from the app layer (to avoid fatal errors from
`registerHeapDynamicHub` trying to link types with missing optional dependencies like
tcnative, Log4j, Jackson), reflection link failures for `ReferenceCountedOpenSslContext`
and `Log4JLogger` became non-fatal warnings.

Final class init instabilities resolved:
- `io.vertx.ext.web.impl.ForwardedParser`
- `io.netty.handler.pcap.PcapWriteHandler$WildcardAddressHolder`
- `io.netty.handler.codec.CharSequenceValueConverter`

### `sun.util.resources.cldr.ext` platform package not in base layer

App layer failed with `Newly seen platform package package sun.util.resources.cldr.ext`.
The `-H:IncludeLocales=en-GB` flag in the app layer pulls in CLDR locale classes from
`jdk.localedata` module, but the base layer only included `module=java.base`.

**Fix:** Added `module=jdk.localedata` to the base layer's `-H:LayerCreate` option:
`-H:LayerCreate=libquarkusbaselayer.nil,module=java.base,module=jdk.localedata${packages}`

### App layer build succeeds

With all workarounds in place, both layers build successfully:
- Base layer: `target/libquarkusbaselayer.so` (309.50MiB), `target/libquarkusbaselayer.nil` (1.57GiB), build time 1m 52s
- App layer: `target/getting-started-1.0.0-SNAPSHOT-runner` (9.69MiB), build time 30.8s

### Mandrel logging changes

Protected the diagnostic logging added earlier behind flags:
- `AnnotationSubstitutionProcessor`: gated behind `-J-Dsvm.traceLinkageErrors=true`
- `NativeImageClassLoader` class load tracing: gated behind `-J-Dsvm.traceClassLoad=true`
- `SVMImageLayerWriter` unlinked type tracing: gated behind `-J-Dsvm.traceLayerTypes=true`
- `SVMImageLayerLoader` relink tracing for specific types: gated behind `-J-Dsvm.traceLayerTypes=true`
- `SVMImageLayerLoader` guarantee failure diagnostics: always on (pre-crash logging)
- `ClassInitializationSupport` demotion tracing: gated behind `-J-Dsvm.traceClassInitDemotions=true`
