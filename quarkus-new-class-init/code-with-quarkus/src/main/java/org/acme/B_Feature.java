package org.acme;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class B_Feature implements Feature {
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        System.out.println("B_Feature.beforeAnalysis");
        final B_InitAtRunTime b = new B_InitAtRunTime();
        System.out.println("Force B to be build time initialized: " + b.toString());
    }
}
