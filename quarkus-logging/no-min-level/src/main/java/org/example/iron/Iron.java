package org.example.iron;

import org.example.main.Asserts;
import org.example.main.Levels;
import org.example.main.Main;
import org.jboss.logging.Logger;

public class Iron
{
    static final Logger LOG = Logger.getLogger(Iron.class);
    static final boolean isTrace = LOG.isTraceEnabled();

    public static void iron()
    {
        tryTraceCached();
        tryTraceOnTheFly();
        justTrace();

        tryInfoOnTheFly();
    }

    private static void tryTraceCached()
    {
        if (isTrace)
        {
            Asserts.assertTraceNotLogged(LOG);
        }
        else
        {
            assert false;
        }
    }

    private static void tryTraceOnTheFly()
    {
        if (LOG.isTraceEnabled())
        {
            Asserts.assertTraceSet();
            Asserts.assertTraceLogged(LOG);
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
            Asserts.assertInfoLogged(LOG);
        }
        else
        {
            Asserts.assertInfoNotSet();
            Asserts.assertInfoNotLogged(LOG);
        }
    }
}
