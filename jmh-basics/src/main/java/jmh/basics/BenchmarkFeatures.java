package jmh.basics;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

@AutomaticFeature
public class BenchmarkFeatures implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        new BenchmarkBeforeAnalysis(
            new BenchmarkBeforeAnalysis.Effects(
                RuntimeReflection::register
                , RuntimeReflection::register
                , access::registerAsUnsafeAccessed
            )
        ).beforeAnalysis();
    }
}
