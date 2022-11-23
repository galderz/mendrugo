FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3.0-java17

COPY ./target/latest_graalvm_home /opt/new-graalvm

ENV PATH="/opt/new-graalvm/bin:$PATH"

