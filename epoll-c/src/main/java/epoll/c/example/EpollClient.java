package epoll.c.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static java.lang.System.out;

public class EpollClient implements AutoCloseable
{
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    final EventLoopGroup group = new EpollEventLoopGroup();
    final CountDownLatch echoLatch = new CountDownLatch(1);

    public void run() throws InterruptedException
    {
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(EpollSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    ChannelPipeline p = ch.pipeline();
                    //p.addLast(new LoggingHandler(LogLevel.INFO));
                    p.addLast(new EchoClientHandler(echoLatch));
                }
            });

        // Start the client.
        b.connect(HOST, PORT).sync();
    }

    public void waitForEcho() throws InterruptedException
    {
        echoLatch.await();
    }

    @Override
    public void close()
    {
        // Shut down the event loop to terminate all threads.
        group.shutdownGracefully();
    }

    static class EchoClientHandler extends ChannelInboundHandlerAdapter
    {
        final CountDownLatch echoLatch;

        EchoClientHandler(CountDownLatch echoLatch)
        {
            this.echoLatch = echoLatch;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx)
        {
            out.println("Channel active, send some data...");
            final var buf = Unpooled.buffer(16);
            buf.writeCharSequence("this is only a test", StandardCharsets.UTF_8);
            ctx.writeAndFlush(buf);
            out.println("Data written and flushed");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
        {
            // Print as received
            ByteBuf buf = (ByteBuf) msg;
            out.printf("<- %s%n", buf.toString(StandardCharsets.UTF_8));
            echoLatch.countDown();
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
