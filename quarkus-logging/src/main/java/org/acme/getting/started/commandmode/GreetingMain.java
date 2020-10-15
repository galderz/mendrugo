package org.acme.getting.started.commandmode;

import javax.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

@QuarkusMain
public class GreetingMain implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(GreetingMain.class);
    private static final boolean isTrace = LOG.isTraceEnabled();

    @Inject
    GreetingService service;

    @Override
    public int run(String... args) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Compute whether trace enabled on the fly");
        }

        if (isTrace) {
            LOG.trace(getMessage());
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
