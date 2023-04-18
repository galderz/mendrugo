FROM fedora:37

# todo do we need to sudo the tar?
RUN dnf -y install  \
    git \
    wget \
  && cd /opt \
  && wget https://github.com/CRaC/openjdk-builds/releases/download/17-crac%2B5/openjdk-17-crac+5_linux-x64.tar.gz \
  && tar xzf openjdk-17-crac+5_linux-x64.tar.gz

ENV JAVA_HOME="/opt/openjdk-17-crac+5_linux-x64"
RUN echo $JAVA_HOME

ENV PATH="$PATH:$JAVA_HOME/bin"
RUN echo $PATH
