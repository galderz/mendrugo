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

Note that by default Quarkus initializes all classes at build-time with exceptions.
This cannot change.

Respect the `JAVA_HOME` environment variable as set currently.

You are free to modify Mandrel or Quarkus source code in order to add debugging information,
but avoid as much as possible modifying logic that affects behaviour unless you've found a clear bug.

If you need to add debugging logic to either Mandrel or Quarkus,
protect it around a system property so that it can be easily disabled without having to revert the debug code logic.

Don't ask for confirmation for any change that requires adding or removing native image parameters to base or app layers,
nor source code changes that add debugging information.
For any other changes, stop to ask for confirmation.

Any changes you make inside Mendrugo, Quarkus or Mandrel, commit them in some kind of logical bundle.

Finally, keep a PROGRESS.md file where you keep track of the different errors, approaches tried and solutions that worked,
so that the logic followed can be reviewed at a later stage.
This will also enable picking specific changes at a later stage if necessary.
