package epoll.c.graal;

import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "libnetty_transport_native_epoll_x86_64", requireStatic = true)
public class Epoll
{
    @CFunction(value = "netty_epoll_native_kernelVersion", transition = CFunction.Transition.NO_TRANSITION)
    static native String kernelVersion(WordBase jnienv, WordBase clazz);
}
