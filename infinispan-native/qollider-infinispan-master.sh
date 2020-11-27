#!/usr/bin/env bash

JDK=${JDK:-https://github.com/openjdk/jdk11u-dev/commit/5ad383f7b802c9e8894e843a18acafd33569979f}
MANDREL=${MANDREL:-https://github.com/graalvm/mandrel/commit/2fc1ec0892574f14133c3796bf88448ccaaea28e}
INFINISPAN=${INFINISPAN:-https://github.com/wburns/infinispan/commit/8dce81940cfef3d18057158112610ddb9493172c}
QUARKUS=${QUARKUS:-https://github.com/galderz/quarkus/commit/c9741eafa7ee6b2f8b2919ba1869fcae2383a60c}
INFINISPAN_QUARKUS=${INFINISPAN_QUARKUS:-https://github.com/galderz/infinispan-quarkus/commit/53ec46a4ed3ce4e9811296bde1fa28560114f7ce}

set -e

source ${HOME}/.dotfiles/qollider/qollider.sh
git pull
JAVA_HOME=/opt/java-14 /opt/maven/bin/mvn clean install

# Uses latest mandrel 20.3 at the time script written
# Infinispan commit is before Will's changes
# Quarkus commit is before my changes to add min-level
/opt/jbang/bin/jbang -Dqollider.version=999-SNAPSHOT https://raw.githubusercontent.com/galderz/qollider/master/examples/infinispan_quarkus.java \
    --jdk ${JDK} \
    --mandrel ${MANDREL} \
    --infinispan ${INFINISPAN} \
    --quarkus ${QUARKUS} \
    --infinispan-quarkus ${INFINISPAN_QUARKUS}
