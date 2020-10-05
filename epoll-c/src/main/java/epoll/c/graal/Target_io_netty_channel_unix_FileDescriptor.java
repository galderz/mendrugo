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

//    @Substitute private static int write(int fd, ByteBuffer buf, int pos, int limit) {return NettyUnixFileDescriptor.(WordFactory.nullPointer(), WordFactory.nullPointer())}

    @Substitute
    private static int writeAddress(int fd, long address, int pos, int limit)
    {
        return NettyUnixFileDescriptor.writeAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, address, pos, limit);
    }

//    @Substitute private static long writev(int fd, ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite) {return NettyUnixFileDescriptor.(WordFactory.nullPointer(), WordFactory.nullPointer())}
//    @Substitute private static long writevAddresses(int fd, long memoryAddress, int length) {return NettyUnixFileDescriptor.(WordFactory.nullPointer(), WordFactory.nullPointer())}
//    @Substitute private static int read(int fd, ByteBuffer buf, int pos, int limit) {return NettyUnixFileDescriptor.(WordFactory.nullPointer(), WordFactory.nullPointer())}

    @Substitute
    private static int readAddress(int fd, long address, int pos, int limit)
    {
        return NettyUnixFileDescriptor.readAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, address, pos, limit);
    }

//    @Substitute private static native long newPipe() {return NettyUnixFileDescriptor.(WordFactory.nullPointer(), WordFactory.nullPointer())}
}
