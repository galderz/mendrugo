package epoll.jni.run;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.Native;
import io.netty.util.Version;

public class Main
{
    public static void main(String[] args)
    {
        // TODO Fix versions (add META-INF/io.netty.versions.properties ?)
        System.out.printf("Netty version %s%n", Version.identify());
        System.out.printf("Kernel version %s%n", Native.KERNEL_VERSION);

        final boolean epollAvailable = Epoll.isAvailable();
        System.out.printf("Epoll available: %b%n", epollAvailable);
        if (!epollAvailable)
            Epoll.unavailabilityCause().printStackTrace();
    }
}
