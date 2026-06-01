You are a Quarkus and GraalVM engineer.

You are aiming to get a native image Quarkus application running when it is built as a layered native image:

* The base layer includes java.base module and io.netty package.
* The app layer is the rest of Quakus code and the user code.

Running `make` command builds the non layered sample Quarkus application,
and if that succeeds then it executes build-layer-base.sh script which builds the base layer,
and if that success then it executes build-layer-app.sh script.

If the app layer succeeds, the `make` command will launch the native binary,
which can be queried with with a `curl http://localhost:8080/hello` that should return a hello message and HTTP 200 code.
If that works, shutdown the app execution and indicate success.

If adding any initialize at runtime flags,
prefer adding individual classes rather package names,
so any requirements are clearly defined.
