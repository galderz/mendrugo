package epoll.c.graal.c;

import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-unix-common", requireStatic = true)
public class NettyUnixLimits
{
    @CFunction(value = "io_netty_unix_limits_ssizeMax", transition = CFunction.Transition.NO_TRANSITION)
    public static native long ssizeMax(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_limits_iovMax", transition = CFunction.Transition.NO_TRANSITION)
    public static native int iovMax(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_limits_uioMaxIov", transition = CFunction.Transition.NO_TRANSITION)
    public static native int uioMaxIov(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_limits_sizeOfjlong", transition = CFunction.Transition.NO_TRANSITION)
    public static native int sizeOfjlong(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_limits_udsSunPathSize", transition = CFunction.Transition.NO_TRANSITION)
    public static native int udsSunPathSize(WordBase jnienv, WordBase clazz);
}
