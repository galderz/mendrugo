package epoll.c.graal.c;

import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

// TODO it's an so, so it's dynamic
// TODO try to remove lib prefix
@CLibrary(value = "libnetty_transport_native_epoll_x86_64", requireStatic = true)
public class NettyUnixLimits
{
    @CFunction(value = "Java_io_netty_channel_unix_LimitsStaticallyReferencedJniMethods_udsSunPathSize", transition = CFunction.Transition.NO_TRANSITION)
    public static native int udsSunPathSize(WordBase jnienv, WordBase clazz);
}
