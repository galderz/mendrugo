package epoll.c.graal.c;

import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-unix-common", requireStatic = true)
public class NettyUnixFileDescriptor
{
//    private static native int open(String path);

    @CFunction(value = "io_netty_unix_filedescriptor_close", transition = CFunction.Transition.NO_TRANSITION)
    public static native int close(WordBase jnienv, WordBase clazz, int fd);

//  @CFunction(value = "io_netty_unix_filedescriptor_", transition = CFunction.Transition.NO_TRANSITION) public static native int write(WordBase jnienv, WordBase clazz, int fd, ByteBuffer buf, int pos, int limit);

    @CFunction(value = "io_netty_unix_filedescriptor_writeAddress", transition = CFunction.Transition.NO_TRANSITION)
    public static native int writeAddress(WordBase jnienv, WordBase clazz, int fd, long address, int pos, int limit);

//  @CFunction(value = "io_netty_unix_filedescriptor_", transition = CFunction.Transition.NO_TRANSITION) public static native long writev(WordBase jnienv, WordBase clazz, int fd, ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite);
//  @CFunction(value = "io_netty_unix_filedescriptor_", transition = CFunction.Transition.NO_TRANSITION) public static native long writevAddresses(WordBase jnienv, WordBase clazz, int fd, long memoryAddress, int length);
//  @CFunction(value = "io_netty_unix_filedescriptor_", transition = CFunction.Transition.NO_TRANSITION) public static native int read(WordBase jnienv, WordBase clazz, int fd, ByteBuffer buf, int pos, int limit);

    @CFunction(value = "io_netty_unix_filedescriptor_readAddress", transition = CFunction.Transition.NO_TRANSITION)
    public static native int readAddress(WordBase jnienv, WordBase clazz, int fd, long address, int pos, int limit);

//  @CFunction(value = "io_netty_unix_filedescriptor_", transition = CFunction.Transition.NO_TRANSITION) public static native long newPipe(WordBase jnienv, WordBase clazz);
}
