package epoll.c;

import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;

public class NettyReflection
{
    public static void register(Reflection reflection)
    {
        reflection.registerMethod.accept(EpollServerSocketChannel.class::getConstructor);
        reflection.registerMethod.accept(EpollSocketChannel.class::getConstructor);
    }

    public static void main(String[] args)
    {
        final var effects = Reflection.of(System.out::println);
        register(effects);
    }
}
