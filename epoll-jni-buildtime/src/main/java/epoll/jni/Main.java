package epoll.jni;

import io.netty.channel.epoll.Epoll;

public class Main
{
    public static void main(String[] args)
    {
        final var epollAvailable = Epoll.isAvailable();
        System.out.println(epollAvailable);
        if (!epollAvailable)
            Epoll.unavailabilityCause().printStackTrace();
    }
}
