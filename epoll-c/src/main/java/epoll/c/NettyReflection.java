package epoll.c;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;

public class NettyReflection
{
    public static void register(Reflection reflection) throws Exception
    {
        registerEpoll(reflection);
        registerNio(reflection);
    }

    private static void registerNio(Reflection reflection) throws ClassNotFoundException
    {
        reflection.registerMethod.accept(NioServerSocketChannel.class::getConstructor);
        reflection.registerMethod.accept(NioSocketChannel.class::getConstructor);
        registerEchoServer(Class.forName("epoll.c.perf.EchoNioServer$EchoServerHandler"), reflection);
    }

    private static void registerEpoll(Reflection reflection) throws ClassNotFoundException
    {
        reflection.registerMethod.accept(EpollServerSocketChannel.class::getConstructor);
        reflection.registerMethod.accept(EpollSocketChannel.class::getConstructor);
        registerInbound(Class.forName("io.netty.channel.DefaultChannelPipeline$TailContext"), reflection);
        registerInbound(Class.forName("io.netty.channel.DefaultChannelPipeline$HeadContext"), reflection);
        registerOutbound(Class.forName("io.netty.channel.DefaultChannelPipeline$HeadContext"), reflection);

        registerServerBootstrapAcceptor(reflection);

        // User-defined
        registerExampleEpollClientHandler(reflection);
        registerExampleEpollServerHandler(reflection);

        registerEchoServer(Class.forName("epoll.c.perf.EchoEpollServer$EchoServerHandler"), reflection);
    }

    private static void registerEchoServer(Class<?> clazz, Reflection reflection) throws ClassNotFoundException
    {
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelRead", ChannelHandlerContext.class, Object.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelReadComplete", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("exceptionCaught", ChannelHandlerContext.class, Throwable.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelWritabilityChanged", ChannelHandlerContext.class)
        );
    }

    private static void registerExampleEpollServerHandler(Reflection reflection) throws ClassNotFoundException
    {
        final Class<?> clazz = Class.forName("epoll.c.example.EpollServer$EchoServerHandler");
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelRead", ChannelHandlerContext.class, Object.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelReadComplete", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("exceptionCaught", ChannelHandlerContext.class, Throwable.class)
        );
    }

    private static void registerExampleEpollClientHandler(Reflection reflection) throws ClassNotFoundException
    {
        final Class<?> clazz = Class.forName("epoll.c.example.EpollClient$EchoClientHandler");
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelActive", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelRead", ChannelHandlerContext.class, Object.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelReadComplete", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("exceptionCaught", ChannelHandlerContext.class, Throwable.class)
        );
    }

    private static void registerServerBootstrapAcceptor(Reflection reflection) throws ClassNotFoundException
    {
        final Class<?> clazz = Class.forName("io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor");
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelRead", ChannelHandlerContext.class, Object.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("exceptionCaught", ChannelHandlerContext.class, Throwable.class)
        );
    }

    private static void registerOutbound(Class<?> clazz, Reflection reflection)
    {
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("bind", ChannelHandlerContext.class, SocketAddress.class, ChannelPromise.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("connect", ChannelHandlerContext.class, SocketAddress.class, SocketAddress.class, ChannelPromise.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("disconnect", ChannelHandlerContext.class, ChannelPromise.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("close", ChannelHandlerContext.class, ChannelPromise.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("deregister", ChannelHandlerContext.class, ChannelPromise.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("read", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("write", ChannelHandlerContext.class, Object.class, ChannelPromise.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("flush", ChannelHandlerContext.class)
        );
    }

    private static void registerInbound(Class<?> clazz, Reflection reflection) throws ClassNotFoundException
    {
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelRegistered", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelUnregistered", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelActive", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelInactive", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelRead", ChannelHandlerContext.class, Object.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelReadComplete", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("channelWritabilityChanged", ChannelHandlerContext.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("userEventTriggered", ChannelHandlerContext.class, Object.class)
        );
        reflection.registerMethod.accept(() ->
            clazz.getDeclaredMethod("exceptionCaught", ChannelHandlerContext.class, Throwable.class)
        );
    }

    public static void main(String[] args) throws Exception
    {
        final var effects = Reflection.of(System.out::println);
        register(effects);
    }
}
