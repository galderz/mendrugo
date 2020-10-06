package epoll.c;

import epoll.c.example.EpollClient;
import epoll.c.example.EpollServer;
import epoll.c.example.NioClient;
import epoll.c.example.NioServer;
import epoll.c.perf.EchoEpollServer;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.Native;
import io.netty.util.Version;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Main
{
    public static void main(String[] args) throws Throwable
    {
        final var run = System.getProperty("run", "epoll-example");
        switch (run)
        {
            case "epoll-example":
                runEpollExample();
                break;
            case "nio-example":
                runNioExample();
                break;
            case "epoll-server":
                runEpollServer();
                break;
        }
    }

    private static void runNioExample() throws Throwable
    {
        System.out.println("Run nio example");
        final var executor = Executors.newSingleThreadExecutor();
        try (var server = new RunNioServer(); var client = new NioClient())
        {
            executor.submit(server).get();
            client.run();
            client.waitForEcho();
        }
        finally
        {
            executor.shutdown();
        }
    }

    private static void runEpollServer()
    {
        EchoEpollServer.main();
    }

    private static void runEpollExample() throws Throwable
    {
        // TODO Fix versions (add META-INF/io.netty.versions.properties ?)
        System.out.printf("Netty version %s%n", Version.identify());
        System.out.printf("Is supporting sendmsg? %b%n", Native.IS_SUPPORTING_SENDMMSG);
        System.out.printf("TCP MD5 signature maximum length %d%n", Native.TCP_MD5SIG_MAXKEYLEN);
        System.out.printf("Kernel version %s%n", Native.KERNEL_VERSION);

        final boolean epollAvailable = Epoll.isAvailable();
        System.out.printf("Epoll available: %b%n", epollAvailable);
        if (!epollAvailable)
        {
            throw Epoll.unavailabilityCause();
        }

        System.out.println("Run epoll example");
        final var executor = Executors.newSingleThreadExecutor();
        try (var server = new RunEpollServer(); var client = new EpollClient())
        {
            executor.submit(server).get();
            client.run();
            client.waitForEcho();
        }
        finally
        {
            executor.shutdown();
        }
    }

    static class RunEpollServer implements Callable<Void>, AutoCloseable
    {
        final EpollServer server = new EpollServer();

        @Override
        public Void call() throws Exception
        {
            server.run();
            return null;
        }

        @Override
        public void close() throws Exception
        {
            server.close();
        }
    }

    static class RunNioServer implements Callable<Void>, AutoCloseable
    {
        final NioServer server = new NioServer();

        @Override
        public Void call() throws Exception
        {
            server.run();
            return null;
        }

        @Override
        public void close() throws Exception
        {
            server.close();
        }
    }
}
