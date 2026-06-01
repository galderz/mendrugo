# Building Mandrel

The Mandrel source code is available as an additional directory at `~/src/mandrel`. The `mx` build tool is at `~/opt/mx`.

## Prerequisites

Set `JAVA_HOME` to the LabsJDK before building. As of 2026-03-31:

```bash
export JAVA_HOME=~/.mx/jdks/labsjdk-ce-latest-jvmci-25.1-b16_amd64
```

The JVMCI version may change when the repository is updated with upstream changes (see "Fetching a new JDK" below).

## Build

```bash
cd ~/src/mandrel/substratevm
~/opt/mx/mx build
```

## Clean

```bash
cd ~/src/mandrel/substratevm
~/opt/mx/mx clean
```

## Fetching a new JDK

When upstream updates require a newer JVMCI version, fetch the new LabsJDK:

```bash
~/opt/mx/mx --quiet fetch-jdk labsjdk-ce-latest
```

This downloads and installs non-interactively to `~/.mx/jdks/`. Then update `JAVA_HOME` to the new path. To list available JDK ids:

```bash
~/opt/mx/mx fetch-jdk --list
```

To check what's currently installed:

```bash
ls ~/.mx/jdks/
```
