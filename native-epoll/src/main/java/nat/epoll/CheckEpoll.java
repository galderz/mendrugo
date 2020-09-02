package nat.epoll;

import io.netty.channel.epoll.Epoll;

public class CheckEpoll
{
    public static void main(String[] args)
    {
        System.out.println(Epoll.isAvailable());
        Epoll.unavailabilityCause().printStackTrace();
    }
}
