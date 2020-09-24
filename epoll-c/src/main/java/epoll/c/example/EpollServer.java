package epoll.c.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;

import static java.lang.System.*;

public class EpollServer implements AutoCloseable
{
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    final EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
    final EventLoopGroup workerGroup = new EpollEventLoopGroup();

    public void run() throws InterruptedException
    {
        // Configure the server.
        final EchoServerHandler serverHandler = new EchoServerHandler();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
            .channel(EpollServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 100)
            // .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch)
                {
                    ChannelPipeline p = ch.pipeline();
                    //p.addLast(new LoggingHandler(LogLevel.INFO));
                    p.addLast(serverHandler);
                }
            });

        // Start the server.
        ChannelFuture f = b.bind(PORT).sync();
        out.println("Server is up!");
    }

    @Override
    public void close()
    {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Sharable
    static class EchoServerHandler extends ChannelInboundHandlerAdapter
    {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
        {
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx)
        {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }
}
