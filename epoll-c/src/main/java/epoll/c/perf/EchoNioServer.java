package epoll.c.perf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.ClosedChannelException;

import static java.lang.System.out;

public class EchoNioServer
{
    private static final int PORT = Integer.parseInt(System.getProperty("port", "8081"));

    public static void main()
    {
        out.println("Run echo nio server");
        final EventLoopGroup group = new NioEventLoopGroup(1);
        final EchoServerHandler serverHandler = new EchoServerHandler();
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                .option(ChannelOption.SO_REUSEADDR, true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception
                    {
                        ChannelPipeline p = ch.pipeline();
                        //p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(serverHandler);
                    }
                });

            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();

            out.println("Server is up!");

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Shut down all event loops to terminate all threads.
            group.shutdownGracefully();
        }
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
            if (cause instanceof IllegalStateException && cause.getCause() != null && cause.getCause() instanceof ClosedChannelException)
            {
                // Close the connection when an exception is raised.
                ctx.close();
            }
            else
            {
                cause.printStackTrace();
                // Close the connection when an exception is raised.
                ctx.close();
            }
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
        {
            // Ensure we are not writing to fast by stop reading if we can not flush out data fast enough.
            if (ctx.channel().isWritable())
            {
                ctx.channel().config().setAutoRead(true);
            }
            else
            {
                ctx.flush();
                if (!ctx.channel().isWritable())
                {
                    ctx.channel().config().setAutoRead(false);
                }
            }
        }
    }
}
