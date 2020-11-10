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
        if (GreetingLogger.isTrace) {
            GreetingLogger.LOG.trace(getTraceCached());
        }

        if (GreetingLogger.LOG.isTraceEnabled()) {
            GreetingLogger.LOG.trace(getTraceOnTheFly());
        }

        if (GreetingLogger.LOG.isInfoEnabled()) {
            GreetingLogger.LOG.info(getInfoOnTheFly());
        }

        GreetingLogger.LOG.trace("Log this message without user checking if trace is enabled");

        if(args.length>0) {
            System.out.println(service.greeting(String.join(" ", args)));
        } else {
            System.out.println(service.greeting("commando"));
        }

        return 0;
    }

    private String getTraceCached() {
        System.out.println("static final boolean isTrace = LOG.isTraceEnabled() = true");
        return "trace()";
    }

    private String getTraceOnTheFly() {
        System.out.println("Runtime log.isTraceEnabled() = true");
        return "trace()";
    }

    private String getInfoOnTheFly() {
        System.out.println("Runtime log.isInfoEnabled() = true");
        return "info()";
    }
}
