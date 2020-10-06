package epoll.c.graal;

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
        List<String> skipThrowables = Arrays.asList(
            "UnsupportedOperationException"
            , "StacklessClosedChannelException"
            , "InterruptedException"
        );

        final String className = this.getClass().getSimpleName();
        boolean doShow = true;

        for (String skipThrowable : skipThrowables)
        {
            if (className.equals(skipThrowable))
            {
                doShow = false;
                break;
            }
        }

        if (doShow)
            StackTraces.show(className, StackTraces.doGetStackTrace());
    }

    @Substitute
    public Target_java_lang_Throwable(String message)
    {
        List<String> skipHeaders = Arrays.asList(
            "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$1"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelRegistered"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelUnregistered"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelActive"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelInactive"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelReadComplete"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelWritabilityChanged"
            , "NoSuchMethodException: io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.userEventTriggered"
            , "NoSuchMethodException: epoll.c.example.EpollClient$1"
            , "NoSuchMethodException: epoll.c.example.EpollClient$EchoClientHandler.channelRegistered"
            , "NoSuchMethodException: epoll.c.example.EpollClient$EchoClientHandler.channelUnregistered"
            , "NoSuchMethodException: epoll.c.example.EpollClient$EchoClientHandler.channelInactive"
            , "NoSuchMethodException: epoll.c.example.EpollClient$EchoClientHandler.channelWritabilityChanged"
            , "NoSuchMethodException: epoll.c.example.EpollClient$EchoClientHandler.userEventTriggered"
            , "NoSuchMethodException: epoll.c.example.EpollServer$1"
            , "NoSuchMethodException: epoll.c.example.EpollServer$EchoServerHandler.channelRegistered"
            , "NoSuchMethodException: epoll.c.example.EpollServer$EchoServerHandler.channelUnregistered"
            , "NoSuchMethodException: epoll.c.example.EpollServer$EchoServerHandler.channelActive"
            , "NoSuchMethodException: epoll.c.example.EpollServer$EchoServerHandler.channelInactive"
            , "NoSuchMethodException: epoll.c.example.EpollServer$EchoServerHandler.channelWritabilityChanged"
            , "NoSuchMethodException: epoll.c.example.EpollServer$EchoServerHandler.userEventTriggered"
            , "NoSuchMethodException: epoll.c.perf.EchoEpollServer$1"
            , "NoSuchMethodException: epoll.c.perf.EchoEpollServer$EchoServerHandler.channelRegistered"
            , "NoSuchMethodException: epoll.c.perf.EchoEpollServer$EchoServerHandler.channelUnregistered"
            , "NoSuchMethodException: epoll.c.perf.EchoEpollServer$EchoServerHandler.channelActive"
            , "NoSuchMethodException: epoll.c.perf.EchoEpollServer$EchoServerHandler.channelInactive"
            , "NoSuchMethodException: epoll.c.perf.EchoEpollServer$EchoServerHandler.userEventTriggered"
            , "FileNotFoundException: /sys/fs/cgroup/memory/memory.limit_in_bytes"

            // TODO: are these valid errors?
            , "NativeIoException: epoll_ctl(..) failed: No such file or directory"
            , "NativeIoException: epoll_ctl(..) failed: Bad file descriptor"
            , "NativeIoException: writeAddress(..) failed: Connection reset by peer"

            , "NoSuchMethodException: epoll.c.example.NioClient$1"
            , "NoSuchMethodException: epoll.c.example.NioServer$1"
            , "NoSuchMethodException: epoll.c.perf.EchoNioServer$1"
            , "NoSuchMethodException: epoll.c.perf.EchoNioServer$EchoServerHandler.channelRegistered"
            , "NoSuchMethodException: epoll.c.perf.EchoNioServer$EchoServerHandler.channelUnregistered"
            , "NoSuchMethodException: epoll.c.perf.EchoNioServer$EchoServerHandler.channelActive"
            , "NoSuchMethodException: epoll.c.perf.EchoNioServer$EchoServerHandler.channelInactive"
            , "NoSuchMethodException: epoll.c.perf.EchoNioServer$EchoServerHandler.userEventTriggered"

            // TODO: are these valid errors?
            , "IOException: Connection reset by peer"
        );

        String header = String.format("%s: %s", this.getClass().getSimpleName(), message);
        // svm does not allow use of lambdas
        boolean doShow = true;
        for (String skipHeader : skipHeaders)
        {
            if (header.contains(skipHeader))
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
            System.out.printf("%s %s%n", vmName, header);
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
