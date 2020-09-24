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
        return NettyEpollNative.eventFd();
    }

//    private static native int timerFd();
//    public static native void eventFdWrite(int fd, long value);
//    public static native void eventFdRead(int fd);
//    static native void timerFdRead(int fd);
//    static native void timerFdSetTime(int fd, int sec, int nsec) throws IOException;

    @Substitute
    private static int epollCreate()
    {
        return NettyEpollNative.epollCreate(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

//    private static native int epollWait0(int efd, long address, int len, int timerFd, int timeoutSec, int timeoutNs);
//    private static native int epollWait(int efd, long address, int len, int timeout);
//    private static native int epollBusyWait0(int efd, long address, int len);
//    private static native int epollCtlAdd0(int efd, int fd, int flags);
//    private static native int epollCtlMod0(int efd, int fd, int flags);
//    private static native int epollCtlDel0(int efd, int fd);
//    private static native int splice0(int fd, long offIn, int fdOut, long offOut, long len);
//    private static native int sendmmsg0(
//        private static native int recvmmsg0(
//        public static native int sizeofEpollEvent();
//    public static native int offsetofEpollData();

}
