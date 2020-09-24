package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import epoll.c.graal.c.NettyUnixFileDescriptor;
import org.graalvm.word.WordBase;
import org.graalvm.word.WordFactory;

@TargetClass(className = "io.netty.channel.unix.FileDescriptor")
final class Target_io_netty_channel_unix_FileDescriptor
{
//    private static native int open(String path);

    @Substitute
    private static int close(int fd)
    {
        return NettyUnixFileDescriptor.close(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

//    private static native int write(int fd, ByteBuffer buf, int pos, int limit);
//    private static native int writeAddress(int fd, long address, int pos, int limit);
//    private static native long writev(int fd, ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite);
//    private static native long writevAddresses(int fd, long memoryAddress, int length);
//    private static native int read(int fd, ByteBuffer buf, int pos, int limit);
//    private static native int readAddress(int fd, long address, int pos, int limit);
//    private static native long newPipe();
}
