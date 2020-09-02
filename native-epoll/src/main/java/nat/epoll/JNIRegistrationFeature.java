package nat.epoll;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class JNIRegistrationFeature implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        try
        {
            defaultFileRegion();
            nativeDatagramPacket();
            peerCredentials();
            datagramSocket();
            JNIRuntimeAccess.register(Class.forName("io.netty.channel.ChannelException"));
        }
        catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void defaultFileRegion() throws ClassNotFoundException, NoSuchFieldException
    {
        final Class<?> clazz = Class.forName("io.netty.channel.DefaultFileRegion");
        JNIRuntimeAccess.register(clazz);
        JNIRuntimeAccess.register(clazz.getField("file"));
        JNIRuntimeAccess.register(clazz.getField("transferred"));
    }

    private void nativeDatagramPacket() throws ClassNotFoundException, NoSuchFieldException
    {
        final Class<?> clazz = Class.forName("io.netty.channel.epoll.NativeDatagramPacketArray$NativeDatagramPacket");
        JNIRuntimeAccess.register(clazz);
        JNIRuntimeAccess.register(clazz.getField("addr"));
        JNIRuntimeAccess.register(clazz.getField("addrLen"));
        JNIRuntimeAccess.register(clazz.getField("scopeId"));
        JNIRuntimeAccess.register(clazz.getField("port"));
        JNIRuntimeAccess.register(clazz.getField("memoryAddress"));
        JNIRuntimeAccess.register(clazz.getField("count"));
    }

    private void peerCredentials() throws ClassNotFoundException, NoSuchMethodException
    {
        final Class<?> clazz = Class.forName("io.netty.channel.unix.PeerCredentials");
        JNIRuntimeAccess.register(clazz);
        JNIRuntimeAccess.register(clazz.getMethod("<init>"));
    }

    private void datagramSocket() throws ClassNotFoundException, NoSuchMethodException
    {
        final Class<?> clazz = Class.forName("io.netty.channel.unix.DatagramSocketAddress");
        JNIRuntimeAccess.register(clazz);
        JNIRuntimeAccess.register(clazz.getMethod("<init>"));
    }
}
