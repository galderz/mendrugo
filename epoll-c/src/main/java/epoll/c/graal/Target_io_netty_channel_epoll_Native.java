package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "io.netty.channel.epoll.Native")
final class Target_io_netty_channel_epoll_Native
{
    @Substitute
    private static void loadNativeLibrary()
    {
        // Don't load library via JNI
    }
}
