package org.example.iron;

import org.example.main.Asserts;
import org.jboss.logging.Logger;

public class Iron
{
    static final Logger LOG = Logger.getLogger(Iron.class);
    static final boolean isTraceStatic = LOG.isTraceEnabled();

    private final boolean isTraceInstance = LOG.isTraceEnabled();

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
