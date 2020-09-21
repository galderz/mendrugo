package epoll.c.graal.c;

import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty_transport_native_epoll_x86_64")
public class NettyUnixLimits
{
    @CFunction(value = "netty_unix_limits_udsSunPathSize", transition = CFunction.Transition.NO_TRANSITION)
    public static native int udsSunPathSize(WordBase jnienv, WordBase clazz);
}
