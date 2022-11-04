package org.acme.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.hub.PredefinedClassesSupport;
import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import org.acme.BaseComponent;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;

@AutomaticFeature
public class ProxyFeature implements Feature
{
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        // PredefinedClassesSupport.registerClass(BaseComponent.class);
        final DynamicProxyRegistry registry = ImageSingletons.lookup(DynamicProxyRegistry.class);
        registry.addProxyClass(BaseComponent.class);
    }
}
