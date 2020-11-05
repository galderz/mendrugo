package org.acme.getting.started.commandmode;

import javax.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class GreetingMain implements QuarkusApplication {

    @Inject
    GreetingService service;

    @Override
    public int run(String... args) {
        if (GreetingLogger.LOG.isTraceEnabled()) {
            GreetingLogger.LOG.trace(getMessageOnTheFly());
        }

        if (GreetingLogger.isTrace) {
            GreetingLogger.LOG.trace(getMessageStaticCached());
        }

        GreetingLogger.LOG.trace("Log this message without user checking if trace is enabled");

        if(args.length>0) {
            System.out.println(service.greeting(String.join(" ", args)));
        } else {
            System.out.println(service.greeting("commando"));
        }

        return 0;
    }

    private String getMessageOnTheFly() {
        System.out.println("Compute message having checked isTraceEnabled at runtime");
        return "Compute whether trace enabled on the fly";
    }

    private String getMessageStaticCached() {
        System.out.println("Compute message having checked isTrace static final field");
        return "Use cached value to see if trace enabled";
    }

}
