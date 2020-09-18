package epoll.jni.run.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import epoll.jni.run.NettyJni;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class NettyJniFeature implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        final var effects = new NettyJni.Effects(
            JNIRuntimeAccess::register
            , JNIRuntimeAccess::register
            , JNIRuntimeAccess::register
        );
        NettyJni.registerJni(effects);
    }
}
