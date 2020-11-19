package org.example.main;

import org.jboss.logging.Logger;

import java.util.function.BiConsumer;

public class Asserts
{
    public static void assertInfoLogged(Logger log)
    {
        assertLogged(true, "info", log::info);
    }

    public static void assertInfoNotLogged(Logger log)
    {
        assertLogged(false, "info", log::info);
    }

    public static void assertTraceLogged(Logger log)
    {
        assertLogged(true, "trace", log::trace);
    }

    public static void assertTraceNotLogged(Logger log)
    {
        assertLogged(false, "trace", log::trace);
    }

    public static void assertInfoSet(Logger log)
    {
        assertLevelSet("Info", log);
    }

    public static void assertInfoNotSet(Logger log)
    {
        assertLevelNotSet("Info", log);
    }

    public static void assertTraceSet(Logger log)
    {
        assertLevelSet("Trace", log);
    }

    public static void assertTraceNotSet(Logger log)
    {
        assertLevelNotSet("Trace", log);
    }

    private static void assertLogged(boolean expected, String msg, BiConsumer<String, Throwable> logFunction)
    {
        try (AssertThrowable t = new AssertThrowable(expected))
        {
            logFunction.accept(msg, t);
        }
    }

    private static void assertLevelSet(String level, Logger log)
    {
        final String levelUpper = level.toUpperCase();
        final String msg = String.format("is%sEnabled returned true but %s level is not set", level, levelUpper);
        assert isSystemPropertyLevel(levelUpper, log) : msg;
    }

    private static void assertLevelNotSet(String level, Logger log)
    {
        final String levelUpper = level.toUpperCase();
        final String msg = String.format("is%sEnabled returned false but %s level is set", level, levelUpper);
        assert !isSystemPropertyLevel(levelUpper, log) : msg;
    }

    public static boolean isSystemPropertyLevel(String level, Logger log)
    {
        final String packageLevel = System.getProperty("quarkus.log.category.\"org.example.table\".level");
        if (packageLevel == null)
        {
            return Levels.isLoggable(level, Main.MIN_LEVEL);
        }
        else
        {
            return Levels.isLoggable(level, packageLevel) && Levels.isLoggable(level, minLevel(log.getName()));
        }
    }

    private static int minLevel(String name)
    {
        switch (name)
        {
            case "org.example.table.Table":
                return Levels.TRACE;
            default:
                return Levels.INFO;
        }
    }

    public static void check()
    {
        boolean enabled = false;
        //noinspection AssertWithSideEffects
        assert enabled = true;
        //noinspection ConstantConditions
        if (!enabled)
        {
            throw new AssertionError("assert not enabled");
        }
    }
}
