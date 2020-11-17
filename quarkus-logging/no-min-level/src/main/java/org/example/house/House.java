package org.example.house;

import org.jboss.logging.Logger;

public class House
{
    static final Logger LOG = Logger.getLogger(House.class);
    static final boolean isTrace = LOG.isTraceEnabled();

    public static void house()
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
            System.out.println("[house] Runtime LOG.isInfoEnabled() = false");
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
            System.out.println("[house] Runtime LOG.isTraceEnabled() = false");
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
            System.out.println("[house] static final boolean isTrace = LOG.isTraceEnabled() = false");
        }
    }

    private static String getTraceCached()
    {
        System.out.println("[house] static final boolean isTrace = LOG.isTraceEnabled() = true");
        return "trace()";
    }

    private static String getTraceOnTheFly()
    {
        System.out.println("[house] Runtime LOG.isTraceEnabled() = true");
        return "trace()";
    }

    private static String getInfoOnTheFly()
    {
        System.out.println("[house] Runtime LOG.isInfoEnabled() = true");
        return "info()";
    }
}
