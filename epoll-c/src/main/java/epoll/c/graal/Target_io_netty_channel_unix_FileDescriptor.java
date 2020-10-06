package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import epoll.c.graal.c.NettyUnixFileDescriptor;
import org.graalvm.word.WordFactory;

import java.nio.ByteBuffer;

@TargetClass(className = "io.netty.channel.unix.FileDescriptor")
final class Target_io_netty_channel_unix_FileDescriptor
{
    @Substitute
    private static int open(String path)
    {
        return NettyUnixFileDescriptor.open(WordFactory.nullPointer(), WordFactory.nullPointer(), JNIObjectHandles.createLocal(path));
    }

    @Substitute
    private static int close(int fd)
    {
        return NettyUnixFileDescriptor.close(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int write(int fd, ByteBuffer buf, int pos, int limit)
    {
        return NettyUnixFileDescriptor.write(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, JNIObjectHandles.createLocal(buf), pos, limit);
    }

    @Substitute
    private static int writeAddress(int fd, long address, int pos, int limit)
    {
        return NettyUnixFileDescriptor.writeAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, address, pos, limit);
    }

    @Substitute
    private static long writev(int fd, ByteBuffer[] buffers, int offset, int length, long maxBytesToWrite)
    {
        return NettyUnixFileDescriptor.writev(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, JNIObjectHandles.createLocal(buffers), offset, length, maxBytesToWrite);
    }

    @Substitute
    private static long writevAddresses(int fd, long memoryAddress, int length)
    {
        return NettyUnixFileDescriptor.writevAddresses(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, memoryAddress, length);
    }

    @Substitute
    private static int read(int fd, ByteBuffer buf, int pos, int limit)
    {
        return NettyUnixFileDescriptor.read(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, JNIObjectHandles.createLocal(buf), pos, limit);
    }

    @Substitute
    private static int readAddress(int fd, long address, int pos, int limit)
    {
        return NettyUnixFileDescriptor.readAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, address, pos, limit);
    }

    @Substitute
    private static long newPipe()
    {
        return NettyUnixFileDescriptor.newPipe(WordFactory.nullPointer(), WordFactory.nullPointer());
    }
}
