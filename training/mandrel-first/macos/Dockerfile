# syntax=docker/dockerfile:1
FROM fedora:34

RUN dnf install -y \
  binutils \
  git \
  perf \
  perl-open

RUN cd /opt \
  && git clone https://github.com/brendangregg/FlameGraph

ENV FG_HOME /opt/FlameGraph

RUN  echo 'alias flamegraph="perf script | ${FG_HOME}/stackcollapse-perf.pl > out.perf-folded && ${FG_HOME}/flamegraph.pl out.perf-folded > flamegraph.svg"' >> ~/.bashrc \
  && echo 'alias kernel-cpu-profiling="echo -1 | sudo tee /proc/sys/kernel/perf_event_paranoid && echo 0 | sudo tee /proc/sys/kernel/kptr_restrict"' >> ~/.bashrc \
  && echo 'alias record="perf record -F 1009 -g -a ./code-with-quarkus-1.0.0-SNAPSHOT-runner"' >> ~/.bashrc \
  && echo 'alias record-dwarf="perf record -F 1009 --call-graph dwarf -a ./code-with-quarkus-1.0.0-SNAPSHOT-runner"' >> ~/.bashrc \
  && echo 'alias report="perf report --stdio"' >> ~/.bashrc \
  && echo 'alias report-srcline="perf report --stdio -F+srcline"' >> ~/.bashrc \
  && echo 'alias query-symbols="objdump -t ./code-with-quarkus-1.0.0-SNAPSHOT-runner | grep"' >> ~/.bashrc \
  && echo 'alias thread-collapse-flamegraph="perf script | sed -E "s/thread-[0-9]*/thread/" | ${FG_HOME}/stackcollapse-perf.pl > out.perf-folded && ${FG_HOME}/flamegraph.pl out.perf-folded > flamegraph.svg"' >> ~/.bashrc

WORKDIR /data
