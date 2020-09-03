package nat.epoll;

import io.netty.channel.epoll.Epoll;

public class CheckEpoll
{
    public static void main(String[] args)
    {
        final var epollAvailable = Epoll.isAvailable();
        System.out.println(epollAvailable);
        if (!epollAvailable)
            Epoll.unavailabilityCause().printStackTrace();
    }
}
