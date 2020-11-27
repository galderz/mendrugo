package org.example.main;

import org.jboss.logging.Logger;

import java.util.function.BiConsumer;

public class Asserts
{
    public static void assertInfoLogged(String message, Logger log)
    {
        assertLogged(true, message, log::info);
    }

    public static void assertInfoNotLogged(Logger log)
    {
        assertLogged(false, "info", log::info);
    }

    public static void assertTraceLogged(String message, Logger log)
    {
        assertLogged(true, message, log::trace);
    }

    public static void assertTraceNotLogged(Logger log)
    {
        assertLogged(false, "trace", log::trace);
    }

    public static void assertInfoSet()
    {
        assertLevelSet("Info");
    }

    public static void assertInfoNotSet()
    {
        assertLevelNotSet("Info");
    }

    public static void assertTraceSet()
    {
        assertLevelSet("Trace");
    }

    public static void assertTraceNotSet()
    {
        assertLevelNotSet("Trace");
    }

    private static void assertLogged(boolean expected, String msg, BiConsumer<String, Throwable> logFunction)
    {
        try (AssertThrowable t = new AssertThrowable(expected))
        {
            logFunction.accept(msg, t);
        }
    }

    private static void assertLevelSet(String level)
    {
        final String levelUpper = level.toUpperCase();
        final String msg = String.format("is%sEnabled returned true but %s level is not set", level, levelUpper);
        assert isSystemPropertyLevel(levelUpper) : msg;
    }

    private static void assertLevelNotSet(String level)
    {
        final String levelUpper = level.toUpperCase();
        final String msg = String.format("is%sEnabled returned false but %s level is set", level, levelUpper);
        assert !isSystemPropertyLevel(levelUpper) : msg;
    }

    private static boolean isSystemPropertyLevel(String level)
    {
        final String packageLevel = System.getProperty("quarkus.log.category.\"org.example.iron\".level");
        if (packageLevel == null)
        {
            return Levels.isLoggable(level, Main.MIN_LEVEL);
        }
        else
        {
            return Levels.isLoggable(level, packageLevel) && Levels.isLoggable(level, Main.MIN_LEVEL);
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
