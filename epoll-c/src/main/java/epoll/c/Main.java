package epoll.c;

import epoll.c.example.EpollClient;
import epoll.c.example.EpollServer;
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
        final var run = System.getProperty("run", "example");
        switch (run)
        {
            case "example":
                runExample();
                break;
            case "epoll-server":
                runEpollServer();
                break;
        }
    }

    private static void runEpollServer()
    {
        EchoEpollServer.main();
    }

    private static void runExample() throws Throwable
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

        System.out.println("Run echo");
        runEcho();
    }

    static void runEcho() throws Exception
    {
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
        EpollServer epollServer = new EpollServer();

        @Override
        public Void call() throws Exception
        {
            epollServer.run();
            return null;
        }

        @Override
        public void close() throws Exception
        {
            epollServer.close();
        }
    }

}
