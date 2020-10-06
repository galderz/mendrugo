package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import epoll.c.graal.c.NettyUnixBuffer;
import org.graalvm.word.WordFactory;

import java.nio.ByteBuffer;

@TargetClass(className = "io.netty.channel.unix.Buffer")
final class Target_io_netty_channel_unix_Buffer
{
    @Substitute
    private static int addressSize0()
    {
        return NettyUnixBuffer.addressSize0(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static long memoryAddress0(ByteBuffer buffer)
    {
        return NettyUnixBuffer.memoryAddress0(WordFactory.nullPointer(), WordFactory.nullPointer(), JNIObjectHandles.createLocal(buffer));
    }
}
