#!/usr/bin/env bash

/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/bin/java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -Dtruffle.TrustAllTruffleRuntimeProviders=true -Dtruffle.TruffleRuntime=com.oracle.truffle.api.impl.DefaultTruffleRuntime -Dgraalvm.ForcePolyglotInvalid=true -Dgraalvm.locatorDisabled=true -Dsubstratevm.IgnoreGraalVersionCheck=true -Djava.lang.invoke.stringConcat=BC_SB --add-exports jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.aarch64=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.services=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.common=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code.stack=ALL-UNNAMED --add-opens jdk.internal.vm.compiler/org.graalvm.compiler.debug=ALL-UNNAMED --add-exports jdk.internal.vm.compiler/org.graalvm.compiler.options=ALL-UNNAMED --add-opens jdk.internal.vm.compiler/org.graalvm.compiler.nodes=ALL-UNNAMED --add-opens jdk.unsupported/sun.reflect=ALL-UNNAMED --add-opens java.base/jdk.internal.module=ALL-UNNAMED --add-opens java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/jdk.internal.reflect=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.ref=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.nio.file=ALL-UNNAMED --add-opens java.base/java.security=ALL-UNNAMED --add-opens java.base/javax.crypto=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens java.base/sun.security.x509=ALL-UNNAMED --add-opens java.base/jdk.internal.logger=ALL-UNNAMED --add-opens org.graalvm.sdk/org.graalvm.nativeimage.impl=ALL-UNNAMED --add-opens org.graalvm.sdk/org.graalvm.polyglot=ALL-UNNAMED --add-opens org.graalvm.truffle/com.oracle.truffle.polyglot=ALL-UNNAMED --add-opens org.graalvm.truffle/com.oracle.truffle.api.impl=ALL-UNNAMED -XX:-UseJVMCICompiler -Xss10m -Xms1g -Xmx13743895344 -Duser.country=US -Duser.language=en -Dorg.graalvm.version=dev -Dorg.graalvm.config= -Dcom.oracle.graalvm.isaot=true --module-path /Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/truffle/truffle-api.jar -javaagent:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/svm.jar -Djdk.internal.lambda.disableEagerInitialization=true -Djdk.internal.lambda.eagerlyInitialize=false -Djava.lang.invoke.InnerClassLambdaMetafactory.initializeLambdas=false -cp /Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/llvm-wrapper-shadowed.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/svm.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/objectfile.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/graal-llvm.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/llvm-platform-specific-shadowed.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/javacpp-shadowed.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/svm-llvm.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/pointsto.jar com.oracle.svm.hosted.NativeImageGeneratorRunner\$JDK9Plus -imagecp /Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/llvm-wrapper-shadowed.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/svm.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/objectfile.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/graal-llvm.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/llvm-platform-specific-shadowed.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/javacpp-shadowed.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/svm-llvm.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/builder/pointsto.jar:/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/library-support.jar:/Users/g/1/graal-19.3/graal/substratevm -H:Path=/Users/g/1/graal-19.3/graal/substratevm -H:CLibraryPath=/Users/g/1/graal-19.3/graal/sdk/mxbuild/darwin-amd64/GRAALVM_LIBGRAAL_JAVA11_BNATIVE-IMAGE_BNATIVE-IMAGE-CONFIGURE_NI_NIC_NIL_NJU_SJVMCICOMPILER_SNATIVE-IMAGE-AGENT/graalvm-libgraal-java11-19.3.1/Contents/Home/lib/svm/clibraries/darwin-amd64,/Users/g/1/graal-19.3/graal/substratevm/mxbuild/darwin-amd64/SVM_HOSTED_NATIVE/darwin-amd64 -H:Class=HelloWorld -H:Name=helloworld
