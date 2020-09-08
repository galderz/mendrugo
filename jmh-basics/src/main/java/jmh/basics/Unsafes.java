package jmh.basics;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class Unsafes implements Feature
{
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        try
        {
            access.registerAsUnsafeAccessed(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL0")
                    .getDeclaredField("markerBegin")
            );
            access.registerAsUnsafeAccessed(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL4")
                    .getDeclaredField("markerEnd")
            );

            access.registerAsUnsafeAccessed(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("type")
            );
            access.registerAsUnsafeAccessed(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("count")
            );
            access.registerAsUnsafeAccessed(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("timeValue")
            );
            access.registerAsUnsafeAccessed(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("batchSize")
            );
        }
        catch (NoSuchFieldException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
