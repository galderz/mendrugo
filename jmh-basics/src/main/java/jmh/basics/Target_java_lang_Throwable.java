package jmh.basics;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TargetClass(Throwable.class)
final class Target_java_lang_Throwable
{
    @Substitute
    public Target_java_lang_Throwable()
    {
        final String header = this.getClass().getSimpleName();
        StackTraces.show(header, StackTraces.doGetStackTrace());
    }

    @Substitute
    public Target_java_lang_Throwable(String message)
    {
        List<String> skip = Arrays.asList(
            "NoSuchMethodException: java.lang.String.valueOf(java.lang.String)"
            , "NoSuchFieldException: markerBegin"
            , "NoSuchFieldException: markerEnd"
            , "NoSuchFieldException: type"
            , "NoSuchFieldException: count"
            , "NoSuchFieldException: timeValue"
            , "NoSuchFieldException: batchSize"
            , "NoSuchFieldException: benchmark"
            , "NoSuchFieldException: generatedTarget"
            , "NoSuchFieldException: synchIterations"
            , "NoSuchFieldException: threads"
            , "NoSuchFieldException: threadGroups"
            , "NoSuchFieldException: forks"
            , "NoSuchFieldException: warmupForks"
            , "NoSuchFieldException: warmup"
            , "NoSuchFieldException: measurement"
            , "NoSuchFieldException: mode"
            , "NoSuchFieldException: params"
            , "NoSuchFieldException: timeUnit"
            , "NoSuchFieldException: opsPerInvocation"
            , "NoSuchFieldException: jvm"
            , "NoSuchFieldException: jvmArgs"
        );

        String header = String.format("%s: %s", this.getClass().getSimpleName(), message);
        // svm does not allow use of lambdas
        boolean doShow = true;
        for (String test : skip)
        {
            if (test.equals(header))
            {
                doShow = false;
                break;
            }
        }

        if (doShow)
            StackTraces.show(header, StackTraces.doGetStackTrace());
    }

    @Substitute
    public StackTraceElement[] getStackTrace()
    {
        // Not accurate but should avoid the exception
        // that stops Quarkus from starting up.
        return StackTraces.doGetStackTrace();
    }

    static final class StackTraces
    {
        static StackTraceElement[] doGetStackTrace()
        {
            return Thread.currentThread().getStackTrace();
        }

        static void show(String header, StackTraceElement[] stackTrace)
        {
            final String vmName = ManagementFactory.getRuntimeMXBean().getName();
            System.out.printf("%s %s", vmName, header);
            show(stackTrace);
        }

        private static void show(StackTraceElement[] stackTrace)
        {
            final String vmName = ManagementFactory.getRuntimeMXBean().getName();
            final String stacktrace = Stream
                .of(stackTrace)
                .map(s -> String.format("%s\tat %s", vmName, s))
                .collect(Collectors.joining("\n"));

            System.out.println(stacktrace);
        }
    }
}
