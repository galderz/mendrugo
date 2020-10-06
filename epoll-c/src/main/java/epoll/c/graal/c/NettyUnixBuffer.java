package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-unix-common", requireStatic = true)
public class NettyUnixBuffer
{
    @CFunction(value = "io_netty_epoll_buffer_addressSize0", transition = CFunction.Transition.NO_TRANSITION)
    public static native int addressSize0(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_epoll_buffer_memoryAddress0", transition = CFunction.Transition.TO_NATIVE)
    public static native long memoryAddress0(WordBase jnienv, WordBase clazz, JNIObjectHandle buffer);
}
