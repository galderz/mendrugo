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

//    private static native int write(int fd, ByteBuffer buf, int pos, int limit);
//    private static native int writeAddress(int fd, long address, int pos, int limit);
//    private static native long writev(int fd, ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite);
//    private static native long writevAddresses(int fd, long memoryAddress, int length);
//    private static native int read(int fd, ByteBuffer buf, int pos, int limit);
//    private static native int readAddress(int fd, long address, int pos, int limit);
//    private static native long newPipe();
}
