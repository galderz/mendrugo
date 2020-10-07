package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-epoll", requireStatic = true, dependsOn = "netty-unix-common")
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
    public static native int eventFd(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_timerFd", transition = CFunction.Transition.NO_TRANSITION)
    public static native int timerFd(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_eventFdWrite", transition = CFunction.Transition.NO_TRANSITION)
    public static native void eventFdWrite(WordBase jnienv, WordBase clazz, int fd, long value);

    @CFunction(value = "io_netty_epoll_native_eventFdRead", transition = CFunction.Transition.NO_TRANSITION)
    public static native void eventFdRead(WordBase jnienv, WordBase clazz, int fd);

    @CFunction(value = "io_netty_epoll_native_timerFdRead", transition = CFunction.Transition.NO_TRANSITION)
    public static native void timerFdRead(WordBase jnienv, WordBase clazz, int fd);

    @CFunction(value = "io_netty_epoll_native_timerFdSetTime", transition = CFunction.Transition.NO_TRANSITION)
    public static native void timerFdSetTime(WordBase jnienv, WordBase clazz, int fd, int sec, int nsec);

    @CFunction(value = "io_netty_epoll_native_epollCreate", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollCreate(WordBase jnienv, WordBase clazz);


    @CFunction(value = "io_netty_epoll_native_epollWait0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollWait0(WordBase jnienv, WordBase clazz, int efd, long address, int len, int timerFd, int timeoutSec, int timeoutNs);

    @CFunction(value = "io_netty_epoll_native_epollWait", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollWait(WordBase jnienv, WordBase clazz, int efd, long address, int len, int timeout);

    @CFunction(value = "io_netty_epoll_native_epollBusyWait0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollBusyWait0(WordBase jnienv, WordBase clazz, int efd, long address, int len);

    @CFunction(value = "io_netty_epoll_native_epollCtlAdd0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollCtlAdd0(WordBase jnienv, WordBase clazz, int efd, int fd, int flags);

    @CFunction(value = "io_netty_epoll_native_epollCtlMod0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollCtlMod0(WordBase jnienv, WordBase clazz, int efd, int fd, int flags);

    @CFunction(value = "io_netty_epoll_native_epollCtlDel0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int epollCtlDel0(WordBase jnienv, WordBase clazz, int efd, int fd);

    @CFunction(value = "io_netty_epoll_native_splice0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int splice0(WordBase jnienv, WordBase clazz, int fd, long offIn, int fdOut, long offOut, long len);

//    @CFunction(value = "io_netty_epoll_native_sendmmsg0", transition = CFunction.Transition.NO_TRANSITION)
//    public static native int sendmmsg0(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle msgs, int offset, int len);
//
//    @CFunction(value = "io_netty_epoll_native_", transition = CFunction.Transition.NO_TRANSITION)
//    public static native int recvmmsg0(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle msgs, int offset, int len);

    @CFunction(value = "io_netty_epoll_native_epollCreate", transition = CFunction.Transition.NO_TRANSITION)
    public static native int sizeofEpollEvent(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_native_offsetofEpollData", transition = CFunction.Transition.NO_TRANSITION)
    public static native int offsetofEpollData(WordBase jnienv, WordBase clazz);

    // <- Native
}
