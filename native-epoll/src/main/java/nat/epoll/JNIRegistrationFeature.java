package nat.epoll;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class JNIRegistrationFeature implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        final var effects = new NettyJNI.Effects(
            JNIRuntimeAccess::register
            , JNIRuntimeAccess::register
            , JNIRuntimeAccess::register
        );
        NettyJNI.registerJNI(effects);
    }
}
