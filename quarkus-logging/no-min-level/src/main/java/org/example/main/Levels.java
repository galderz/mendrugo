package org.example.main;

public class Levels
{
    public static final int ERROR = 1000;
    public static final int WARN = 900;
    public static final int INFO = 800;
    public static final int DEBUG = 500;
    public static final int TRACE = 400;

    public static boolean isLoggable(String level, String minLevel)
    {
        return asIntLevel(level) >= asIntLevel(minLevel);
    }

    private static int asIntLevel(String level)
    {
        switch (level)
        {
            case "ERROR": return ERROR;
            case "WARN": return WARN;
            case "INFO": return INFO;
            case "DEBUG": return DEBUG;
            case "TRACE": return TRACE;
            default:
                throw new RuntimeException("Unknown level: " + level);
        }
    }
}
