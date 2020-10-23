package org.acme.getting.started.commandmode;

import org.jboss.logging.Logger;

public class GreetingLogger
{
    static final Logger LOG = Logger.getLogger(GreetingLogger.class);
    static final boolean isTrace = LOG.isTraceEnabled();
}
