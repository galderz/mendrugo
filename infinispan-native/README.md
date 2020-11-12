# Requirements

* Checkout
[dotfiles](https://github.com/galderz/dotfiles/blob/master/qollider/qollider.sh)
repo under `${HOME}/.dotfiles` folder.
* Checkout
[qollider](https://github.com/galderz/qollider)
repo under `${HOME}/1/qollider`
* Java 11 and Maven in client.
* [Flamegraph](https://github.com/brendangregg/FlameGraph).
By default, the code expects to be under `/opt/Flamegraph` but it's configurable.

# Prepare servers

Grab all the bits and build them together:

```bash
$ ./qollider-infinispan-master.sh
```

As a end product, the script above creates 2 base Infinispan native servers:

* The first one has native debugging enabled,
which means that it creates DWARF symbols that one can use to do native step-by-step debugging with tools like `gdb`.
This first native server also builds with `-H:-DeleteLocalSymbols` and `-H:+PreserveFramePointer` flags,
so that detailed native profiling information can be obtained with `perf`.
* The second server has no debug or extra profiling options added.
This server can be used to get raw performance numbers and do comparisons.

**NOTE:** Builds master branches for Infinispan and Quarkus,
but then currently this
[infinispan-quarkus](https://github.com/galderz/infinispan-quarkus/commits/t_fix_cli_java_26)
branch to avoid
[infinispan-quarkus#26](https://github.com/infinispan/infinispan-quarkus/issues/26)
and
[infinispan-quarkus#38](https://github.com/infinispan/infinispan-quarkus/issues/38)
issues.

Next, prepare the servers for running them.
This involves adding a user/password combination,
as well as creating 2 set of directories for different clusters nodes.

```bash
$ make install-servers
```

# Set environment

If you're going to talk to the server from a different machine,
creating an `.env` file with the address to which the server should bind.
This file can also be used by the client to choose which server to talk to initially:

```bash
export ADDRESS=192.168.1.101
```

# Smoke test

A simple smoke test to verify that a client can connect to the server can be done the following way:

In the server, set the environment variables and start the native server:

```bash
$ source .env
$ make start-native1
cd /tmp/servers-$(date +%d%m)/native1 && ./infinispan-quarkus-server-runner-12.0.0-SNAPSHOT-runner "" -Dquarkus.infinispan-server.bind-port=11222 -Dinfinispan.bind.address=192.168.1.101
...
2020-11-12 07:56:19,652 INFO  [io.quarkus] (main) infinispan-quarkus-server-runner 12.0.0-SNAPSHOT native (powered by Quarkus 999-SNAPSHOT) started in 2.111s.
```

In the client, set the environment variables and store some data in the server.
Remember to have Java 11 and `mvn` in the path before executing this:

```bash
$ source .env
$ NUM_ENTRIES=10 make load-data
```

# Debugging with gdb

Debugging native code can be done directly with `gdb`
or via such as `emacs`.
The latter has the benefit of opening source code side by side when a breakpoint is reached.
In this case, `emacs` will be used.
To debug, go to the server machine, `cd` into the native server created in the prepare servers step and start `emacs`:

```bash
$ cd /tmp/servers-*/tracing-native1
$ emacs
```

Once inside emacs,
start a `gud-gdb` session instructing to executing the native executable for the server.
If everything is set correctly, you should see a message that says that symbols have been read correctly:

```
M-x gud-gdb
Run gud-gdb (like this): gdb --fullname infinispan-quarkus-server-runner-12.0.0-SNAPSHOT-runner
...
Reading symbols from infinispan-quarkus-server-runner-12.0.0-SNAPSHOT-runner...
Reading symbols from /tmp/servers-1211/tracing-native1/infinispan-quarkus-server-runner-12.0.0-SNAPSHOT-runner.debug...
```

Next, set the arguments to be passed to the Infinispan native server for execution:

```
(gdb) set args "" -Dquarkus.infinispan-server.bind-port=11222 -Dinfinispan.bind.address=192.168.1.101
```

Now try to look up a function present in Infinispan.
`visitCommand` is a good example to try since `InvocationContextInterceptor::visitCommand`
is a good place to start when trying to step through Infinispan execution.
When you lookup `visitCommand` you should see the implementation in `InvocationContextInterceptor` appearing:

```
(gdb) info func visitCommand
...
File org/infinispan/interceptors/impl/InvocationContextInterceptor.java:
        void org.infinispan.interceptors.impl.InvocationContextInterceptor::visitCommand(org.infinispan.context.InvocationContext, org.infinispan.commands.VisitableCommand)(void);
```

Next, you can try to put a breakpoint in that method:

```
(gdb) break org.infinispan.interceptors.impl.InvocationContextInterceptor::visitCommand
```

Finally, instruct `gdb` to run the server.
Shortly after you should see the breakpoint being hit as commands begin to flow through Infinispan:

```
(gdb) run
...
Thread 1 "infinispan-quar" hit Breakpoint 1, org.infinispan.interceptors.impl.InvocationContextInterceptor::visitCommand(org.infinispan.context.InvocationContext, org.infinispan.commands.VisitableCommand)(void) ()                                                                  
    at org/infinispan/interceptors/impl/InvocationContextInterceptor.java:81
81               log.tracef("Invoked with command %s and InvocationContext [%s]", command, ctx);
```

If running on `emacs`,
you should see the window split and the source code appearing in the other panel.
By hitting `n` or `s` in the `gdb` console you can step-over or step-into functions.
Hitting `c` the program continues.
As next steps, you can try to locate the put key/value command, set a breakpoint there and instruct the client to store some key/value pairs.

**NOTE:** No type information is available yet.
Hence, step-by-step debugging is mostly suited today for figuring out code paths leading to crashes.

**NOTE:** Bear in mind that,
some paths the debugger takes might seem not strictly sequential when compared with the source code.
Also, some paths might lead to jumps into internal GraalVM or JDK source code unexpectedly (e.g. GC or Graal compiler code).

# Native profiling

To profile natively, the native execution needs to be piped throuhg `perf record`.
That's automatically done when calling:

```bash
$ make record-native1
cd /tmp/servers-$(date +%d%m)/tracing-native1 && \
	/usr/bin/perf record -F 99 -a --call-graph dwarf -- ./infinispan-quarkus-server-runner-12.0.0-SNAPSHOT-runner "" -Dquarkus.infinispan-server.bind-port=11222 -Dinfinispan.bind.address=192.168.1.163
...
2020-11-12 09:56:06,565 INFO  [io.quarkus] (main) infinispan-quarkus-server-runner 12.0.0-SNAPSHOT native (powered by Quarkus 999-SNAPSHOT) started in 2.103s.
```

Exercise the server then using the client code available in the repo.
Up the number of entries to further stress the system.

Then, stop the server which will generate a `perf.data`:

```bash
...
^C2020-11-12 09:57:37,594 WARN  [org.inf.SERVER] (Shutdown thread) Could not register the ServerInitialContextFactoryBuilder. JNDI will not be available
[ perf record: Woken up 1 times to write data ]
2020-11-12 09:57:37,599 INFO  [ListenerBean] (Shutdown thread) The application is stopping...
2020-11-12 09:57:37,599 INFO  [io.quarkus] (Shutdown thread) infinispan-quarkus-server-runner stopped in 0.005s
[ perf record: Captured and wrote 2.532 MB perf.data (634 samples) ]
```

Finally, convert the `perf.data` into a flamegraph `svg` file under the temporary server directory:

```bash
$ make gen-graphs
/usr/bin/perf script -i /tmp/servers-$(date +%d%m)/tracing-native1/perf.data | /opt/FlameGraph/stackcollapse-perf.pl > /tmp/servers-$(date +%d%m)/graphs/out.perf-folded
/opt/FlameGraph/flamegraph.pl /tmp/servers-$(date +%d%m)/graphs/out.perf-folded > /tmp/servers-$(date +%d%m)/graphs/native1-perf-kernel.svg
```
