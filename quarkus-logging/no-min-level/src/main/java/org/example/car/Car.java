package org.example.car;

import org.jboss.logging.Logger;

public class Car
{
    static final Logger LOG = Logger.getLogger(Car.class);
    static final boolean isTrace = LOG.isTraceEnabled();

    public static void car()
    {
        tryTraceCached();
        tryTraceOnTheFly();
        traceOnTheFly();
        tryInfoOnTheFly();
    }

    private static void tryInfoOnTheFly()
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(getInfoOnTheFly());
        }
        else
        {
            System.out.println("[car] Runtime LOG.isInfoEnabled() = false");
        }
    }

    private static void traceOnTheFly()
    {
        LOG.trace("Log this message without user checking if trace is enabled");
    }

    private static void tryTraceOnTheFly()
    {
        if (LOG.isTraceEnabled())
        {
            LOG.trace(getTraceOnTheFly());
        }
        else
        {
            System.out.println("[car] Runtime LOG.isTraceEnabled() = false");
        }
    }

    private static void tryTraceCached()
    {
        if (isTrace)
        {
            LOG.trace(getTraceCached());
        }
        else
        {
            System.out.println("[car] static final boolean isTrace = LOG.isTraceEnabled() = false");
        }
    }

    private static String getTraceCached()
    {
        System.out.println("[car] static final boolean isTrace = LOG.isTraceEnabled() = true");
        return "trace()";
    }

    private static String getTraceOnTheFly()
    {
        System.out.println("[car] Runtime LOG.isTraceEnabled() = true");
        return "trace()";
    }

    private static String getInfoOnTheFly()
    {
        System.out.println("[car] Runtime LOG.isInfoEnabled() = true");
        return "info()";
    }
}
