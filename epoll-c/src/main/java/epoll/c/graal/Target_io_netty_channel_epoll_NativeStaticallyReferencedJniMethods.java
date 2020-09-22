package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import com.oracle.svm.jni.JNIThreadLocalEnvironment;
import epoll.c.graal.c.NettyEpollNative;
import org.graalvm.word.WordFactory;

@TargetClass(className = "io.netty.channel.epoll.NativeStaticallyReferencedJniMethods")
final class Target_io_netty_channel_epoll_NativeStaticallyReferencedJniMethods
{
    @Substitute
    static int epollin()
    {
        return NettyEpollNative.epollin(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static int epollout()
    {
        return NettyEpollNative.epollout(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static int epollrdhup()
    {
        return NettyEpollNative.epollrdhup(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static int epollet()
    {
        return NettyEpollNative.epollet(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static int epollerr()
    {
        return NettyEpollNative.epollerr(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static int tcpMd5SigMaxKeyLen()
    {
        return NettyEpollNative.tcpMd5SigMaxKeyLen(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static boolean isSupportingSendmmsg()
    {
        return NettyEpollNative.isSupportingSendmmsg(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static boolean isSupportingRecvmmsg()
    {
        return NettyEpollNative.isSupportingRecvmmsg(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static boolean isSupportingTcpFastopen()
    {
        return NettyEpollNative.isSupportingTcpFastopen(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    static String kernelVersion()
    {
        System.out.println("kernelVersion() begin");

        return JNIObjectHandles.getObject(
            NettyEpollNative.kernelVersion(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer())
        );
    }
}
