package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-epoll", requireStatic = true)
public class NettyEpollNative
{
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
}
