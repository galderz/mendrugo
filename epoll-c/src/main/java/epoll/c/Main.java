package epoll.c;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.Native;
import io.netty.util.Version;

public class Main
{
    public static void main(String[] args)
    {
        // TODO Fix versions (add META-INF/io.netty.versions.properties ?)
        System.out.printf("Netty version %s%n", Version.identify());
        System.out.printf("Is supporting sendmsg? %b%n", Native.IS_SUPPORTING_SENDMMSG);
        System.out.printf("TCP MD5 signature maximum length %d%n", Native.TCP_MD5SIG_MAXKEYLEN);
        System.out.printf("Kernel version %s%n", Native.KERNEL_VERSION);

//        final boolean epollAvailable = Epoll.isAvailable();
//        System.out.printf("Epoll available: %b%n", epollAvailable);
//        if (!epollAvailable)
//            Epoll.unavailabilityCause().printStackTrace();
    }
}
