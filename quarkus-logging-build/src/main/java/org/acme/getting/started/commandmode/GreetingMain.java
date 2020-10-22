package org.acme.getting.started.commandmode;

import javax.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

@QuarkusMain
public class GreetingMain implements QuarkusApplication {

    @Inject
    GreetingService service;

    @Override
    public int run(String... args) {
        if (GreetingLogger.LOG.isTraceEnabled()) {
            GreetingLogger.LOG.trace("Compute whether trace enabled on the fly");
        }

        if (GreetingLogger.isTrace) {
            GreetingLogger.LOG.trace(getMessage());
        }

        if(args.length>0) {
            System.out.println(service.greeting(String.join(" ", args)));
        } else {
            System.out.println(service.greeting("commando"));
        }

        return 0;
    }

    private String getMessage() {
        System.out.println("Working very hard to provide a trace message");
        return "Use cached value to see if trace enabled";
    }

}
