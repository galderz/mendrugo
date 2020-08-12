FROM registry.access.redhat.com/ubi8/ubi-minimal:8.0

RUN microdnf -y install \
        gcc \
        gcc-c++ \
        glibc-devel \
        gzip \
        shadow-utils \
        tar \
        unzip \
        zlib-devel

RUN mkdir -p /opt \
    && curl -SL https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-20.1.0/graalvm-ce-java11-linux-amd64-20.1.0.tar.gz \
    | tar -xzpC /opt \
    && mv /opt/graalvm-ce-java11-20.1.0 /opt/graalvm

RUN /opt/graalvm/bin/gu --auto-yes install native-image

RUN mkdir /data

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENV PATH="/opt/graalvm/bin:${PATH}"
