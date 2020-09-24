package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-epoll", requireStatic = true)
public class NettyEpollNative
{
    // NativeStaticallyReferencedJniMethods ->

    @CFunction(value = "io_netty_epoll_native_epollin", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollin(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_epollout", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollout(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_epollrdhup", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollrdhup(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_epollet", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollet(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_epollerr", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollerr(WordBase jnienv, WordBase clazz);

//    static native long ssizeMax();

    @CFunction(value = "io_netty_epoll_native_isSupportingSendmmsg", transition = CFunction.Transition.NO_TRANSITION)
    public static native int tcpMd5SigMaxKeyLen(WordBase jnienv, WordBase clazz);

//    static native int iovMax();
//    static native int uioMaxIov();

    @CFunction(value = "io_netty_epoll_native_isSupportingSendmmsg", transition = CFunction.Transition.NO_TRANSITION)
    public static native boolean isSupportingSendmmsg(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_epollerr", transition = CFunction.Transition.NO_TRANSITION)
    public static native boolean isSupportingRecvmmsg(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_epollerr", transition = CFunction.Transition.NO_TRANSITION)
    public static native boolean isSupportingTcpFastopen(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_kernelVersion", transition = CFunction.Transition.TO_NATIVE)
    public static native JNIObjectHandle kernelVersion(WordBase jnienv, WordBase clazz);

    // <- NativeStaticallyReferencedJniMethods

    // Native ->

    @CFunction(value = "io_netty_epoll_native_eventFd", transition = CFunction.Transition.NO_TRANSITION)
    public static native int eventFd();

//    private static native int timerFd();
//    public static native void eventFdWrite(int fd, long value);
//    public static native void eventFdRead(int fd);
//    static native void timerFdRead(int fd);
//    static native void timerFdSetTime(int fd, int sec, int nsec) throws IOException;

    @CFunction(value = "io_netty_epoll_native_epollCreate", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollCreate(WordBase jnienv, WordBase clazz);

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

    // <- Native
}
