package org.example.table;

import org.example.main.Asserts;
import org.jboss.logging.Logger;

public class Table
{
    static final Logger LOG = Logger.getLogger(Table.class);
    static final boolean isTrace = LOG.isTraceEnabled();

    public static void table()
    {
        tryTraceCached();
        tryTraceOnTheFly();
        justTrace();

        tryInfoOnTheFly();
    }

    private static void tryTraceCached()
    {
        if (isTrace) // always returns true
        {
            tryTraceOnTheFly();
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
            Asserts.assertTraceSet(LOG);
            Asserts.assertTraceLogged(LOG);
        }
        else
        {
            Asserts.assertTraceNotSet(LOG);
            Asserts.assertTraceNotLogged(LOG);
        }
    }

    private static void justTrace()
    {
        if (Asserts.isSystemPropertyLevel("TRACE", LOG))
        {
            Asserts.assertTraceSet(LOG);
            Asserts.assertTraceLogged(LOG);
        }
        else
        {
            Asserts.assertTraceNotSet(LOG);
            Asserts.assertTraceNotLogged(LOG);
        }
    }

    private static void tryInfoOnTheFly()
    {
        if (LOG.isInfoEnabled())
        {
            Asserts.assertInfoSet(LOG);
            Asserts.assertInfoLogged(LOG);
        }
        else
        {
            Asserts.assertInfoNotSet(LOG);
            Asserts.assertInfoNotLogged(LOG);
        }
    }
}
