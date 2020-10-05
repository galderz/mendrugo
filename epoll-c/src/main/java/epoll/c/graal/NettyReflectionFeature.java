package epoll.c.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import epoll.c.NettyReflection;
import epoll.c.Reflection;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

@AutomaticFeature
public class NettyReflectionFeature implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        final var effects = Reflection.of(
            RuntimeReflection::register
        );
        try
        {
            NettyReflection.register(effects);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
