package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import epoll.c.graal.c.NettyUnixLimits;
import org.graalvm.word.WordFactory;

@TargetClass(className = "io.netty.channel.unix.LimitsStaticallyReferencedJniMethods")
final class Target_io_netty_channel_unix_LimitsStaticallyReferencedJniMethods
{
    @Substitute
    static int udsSunPathSize()
    {
        return NettyUnixLimits.udsSunPathSize(WordFactory.nullPointer(), WordFactory.nullPointer());
    }
}
