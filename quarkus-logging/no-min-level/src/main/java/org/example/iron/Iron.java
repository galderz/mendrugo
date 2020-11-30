package org.example.iron;

import org.example.main.Asserts;
import org.jboss.logging.Logger;

public class Iron
{
    static final Logger LOG = Logger.getLogger(Iron.class);
    static final boolean isTraceStatic = LOG.isTraceEnabled();

    private final boolean isTraceInstance = LOG.isTraceEnabled();

    public Logger getLog()
    {
        return LOG;
    }

    public void iron()
    {
        tryTraceCachedStatic();
        this.tryTraceCacheInstance();

        tryTraceOnTheFly();
        justTrace();

        tryInfoOnTheFly();
    }

    private static void tryTraceCachedStatic()
    {
        if (isTraceStatic)
        {
            Asserts.assertTraceNotLogged(LOG);
        }
        else
        {
            assert false;
        }
    }

    private void tryTraceCacheInstance()
    {
        if (isTraceInstance)
        {
            Asserts.assertTraceSet();
            Asserts.assertTraceLogged("iron-trace-instance-fly", LOG);
        }
        else
        {
            Asserts.assertTraceNotSet();
            Asserts.assertTraceNotLogged(LOG);
        }

        if (getLog().isTraceEnabled())
        {
            Asserts.assertTraceSet();
            Asserts.assertTraceLogged("iron-trace-instance-getlog-fly", LOG);
        }
    }

    private static void tryTraceOnTheFly()
    {
        if (LOG.isTraceEnabled())
        {
            Asserts.assertTraceSet();
            Asserts.assertTraceLogged("iron-trace-fly", LOG);
        }
        else
        {
            Asserts.assertTraceNotSet();
            Asserts.assertTraceNotLogged(LOG);
        }

        final long start = System.currentTimeMillis();
        if (start == 0 && LOG.isTraceEnabled())
        {
            Asserts.assertTraceSet();
            Asserts.assertTraceLogged("iron-trace-conditional-start-fly", LOG);
        }

        final long end = System.currentTimeMillis();
        if (LOG.isTraceEnabled() && end == 0)
        {
            Asserts.assertTraceSet();
            Asserts.assertTraceLogged("iron-trace-conditional-end-fly", LOG);
        }
    }

    private static void justTrace()
    {
        Asserts.assertTraceNotSet();
        Asserts.assertTraceNotLogged(LOG);
    }

    private static void tryInfoOnTheFly()
    {
        if (LOG.isInfoEnabled())
        {
            Asserts.assertInfoSet();
            Asserts.assertInfoLogged("iron-info-fly", LOG);
        }
        else
        {
            Asserts.assertInfoNotSet();
            Asserts.assertInfoNotLogged(LOG);
        }
    }
}
