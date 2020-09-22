package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import epoll.c.graal.c.NettyUnixSocket;

@TargetClass(className = "io.netty.channel.unix.Socket")
final class Target_io_netty_channel_unix_Socket
{
    @Substitute
    static void initialize(boolean ipv4Preferred)
    {
        NettyUnixSocket.initialize(ipv4Preferred);
    }
}
