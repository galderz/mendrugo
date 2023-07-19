# Increased RSS debugging blog post

Start by verifying you can run both apps in JVM mode:

```bash
$ cd before
$ make run-jvm
...
$ cd after
$ make run-jvm
```

Then, check if you can run in native mode before the RSS fix,
and check what the RSS consumption is:

```bash
$ cd before
$ make
...
$ make rss
ps -p $(pidof getting-started-1.0.0-SNAPSHOT-runner) -o rss=
33024
```

Do the same after the RSS fix and compare values:

```bash
$ cd after
$ make
...
$ make rss
ps -p $(pidof getting-started-1.0.0-SNAPSHOT-runner) -o rss=
36608
```
