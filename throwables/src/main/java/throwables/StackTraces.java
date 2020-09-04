package throwables;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StackTraces
{
    static void show(String message, StackTraceElement[] stackTrace, String className)
    {
        if (message != null)
            System.out.printf("%s: %s%n", className, message);

        final var stacktrace = Stream
            .of(stackTrace)
            .map(s -> String.format("\tat %s", s))
            .collect(Collectors.joining("\n"));

        System.out.println(stacktrace);
    }

    static StackTraceElement[] getStackTrace()
    {
        return Thread.currentThread().getStackTrace();
    }

    public static void main(String[] args)
    {
        StackTraces.show(null, getStackTrace(), StackTraces.class.getSimpleName());
    }
}
