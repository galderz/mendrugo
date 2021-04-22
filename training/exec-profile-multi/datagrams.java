///usr/bin/env jbang "$0" "$@" ; exit $?
// //DEPS <dependency1> <dependency2>

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.*;

public class datagrams
{
    private static final AtomicLong totalPackets = new AtomicLong();
    private static DatagramChannel ch;

    public static void sendLoop()
    {
        final ByteBuffer buf = ByteBuffer.allocateDirect(1000);
        final InetSocketAddress remoteAddr = new InetSocketAddress("127.0.0.1", 5556);

        try
        {
            while (true)
            {
                buf.clear();
                ch.send(buf, remoteAddr);
                totalPackets.incrementAndGet();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws Exception
    {
        ch = DatagramChannel.open();
        ch.bind(new InetSocketAddress(5555));
        ch.configureBlocking(false);

        Executor pool = Executors.newCachedThreadPool(new ShortNameThreadFactory());
        for (int i = 0; i < 10; i++)
        {
            pool.execute(datagrams::sendLoop);
        }

        System.out.println("Warming up...");
        Thread.sleep(3000);
        totalPackets.set(0);

        System.out.println("Benchmarking...");
        Thread.sleep(5000);
        System.out.println(totalPackets.get() / 5);

        System.exit(0);
    }

    private static final class ShortNameThreadFactory implements ThreadFactory
    {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "thread-";

        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r,
                namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
