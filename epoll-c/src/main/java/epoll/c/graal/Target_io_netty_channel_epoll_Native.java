package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import epoll.c.graal.c.NettyEpollNative;
import org.graalvm.word.WordFactory;

@TargetClass(className = "io.netty.channel.epoll.Native")
final class Target_io_netty_channel_epoll_Native
{
    @Substitute
    private static void loadNativeLibrary()
    {
        // Don't load library via JNI
    }

    @Substitute
    private static int eventFd()
    {
        return NettyEpollNative.eventFd(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int timerFd()
    {
        return NettyEpollNative.timerFd(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

//    @Substitute
//    private static void eventFdWrite(int fd, long value) {}
//    @Substitute
//    private static void eventFdRead(int fd) {}
//    @Substitute
//    private static void timerFdRead(int fd) {}
//    @Substitute
//    private static void timerFdSetTime(int fd, int sec, int nsec) throws IOException {}

    @Substitute
    private static int epollCreate()
    {
        return NettyEpollNative.epollCreate(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

//    @Substitute private static int epollWait0(int efd, long address, int len, int timerFd, int timeoutSec, int timeoutNs) {}

    @Substitute
    private static int epollWait(int efd, long address, int len, int timeout)
    {
        return NettyEpollNative.epollWait(WordFactory.nullPointer(), WordFactory.nullPointer(), efd, address, len, timeout);
    }

    @Substitute
    private static int epollBusyWait0(int efd, long address, int len)
    {
        return NettyEpollNative.epollBusyWait0(WordFactory.nullPointer(), WordFactory.nullPointer(), efd, address, len);
    }

    @Substitute
    private static int epollCtlAdd0(int efd, int fd, int flags)
    {
        return NettyEpollNative.epollCtlAdd0(WordFactory.nullPointer(), WordFactory.nullPointer(), efd, fd, flags);
    }

    @Substitute
    private static int epollCtlMod0(int efd, int fd, int flags)
    {
        return NettyEpollNative.epollCtlMod0(WordFactory.nullPointer(), WordFactory.nullPointer(), efd, fd, flags);
    }

//    @Substitute
//    private static int epollCtlDel0(int efd, int fd) {}
//    @Substitute
//    private static int splice0(int fd, long offIn, int fdOut, long offOut, long len) {}
//    @Substitute
//    private static int sendmmsg0 {}
//    @Substitute
//    private static int recvmmsg0 {}

    @Substitute
    private static int sizeofEpollEvent()
    {
        return NettyEpollNative.sizeofEpollEvent(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    public static int offsetofEpollData()
    {
        return NettyEpollNative.offsetofEpollData(WordFactory.nullPointer(), WordFactory.nullPointer());
    }
}
