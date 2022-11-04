Fails with:
```bash
mvn package -Dnative -DskipTests \
    -Dquarkus.native.additional-build-args=-J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk.proxy=ALL-UNNAMED,-J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.hub=ALL-UNNAMED
```

Runs fine with:
```bash
mvn package -Dnative -DskipTests \
    -Dquarkus.native.additional-build-args=-J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk.proxy=ALL-UNNAMED,-J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.hub=ALL-UNNAMED,--initialize-at-run-time=org.acme.ImageData
```