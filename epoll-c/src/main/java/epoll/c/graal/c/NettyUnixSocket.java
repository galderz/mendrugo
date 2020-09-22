package epoll.c.graal.c;

import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;

@CLibrary(value = "netty-unix-common", requireStatic = true)
public class NettyUnixSocket
{
    @CFunction(value = "io_netty_unix_socket_initialize", transition = CFunction.Transition.NO_TRANSITION)
    public static native void initialize(boolean ipv4Preferred);
}
