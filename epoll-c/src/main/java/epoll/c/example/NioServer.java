package epoll.c.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static java.lang.System.out;

public class NioServer implements AutoCloseable
{
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void run() throws InterruptedException
    {
        // Configure the server.
        final EpollServer.EchoServerHandler serverHandler = new EpollServer.EchoServerHandler();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
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

    @ChannelHandler.Sharable
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
