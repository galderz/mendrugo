package mendrugo.infinispan.embedded;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.util.Arrays;

@AutomaticFeature
public class RuntimeReflectionRegistrationFeature implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        RuntimeReflection.register(org.infinispan.commons.logging.Log.class);
        RuntimeReflection.register(org.infinispan.commons.logging.Log.class.getConstructors());
        RuntimeReflection.register(org.infinispan.commons.logging.Log_$logger.class);
        RuntimeReflection.register(org.infinispan.commons.logging.Log_$logger.class.getConstructors());

        RuntimeReflection.register(org.infinispan.util.logging.Log.class);
        RuntimeReflection.register(org.infinispan.util.logging.Log.class.getConstructors());
        RuntimeReflection.register(org.infinispan.util.logging.Log_$logger.class);
        RuntimeReflection.register(org.infinispan.util.logging.Log_$logger.class.getConstructors());

        RuntimeReflection.register(org.infinispan.distribution.ch.impl.HashFunctionPartitioner.class.getConstructors());

        RuntimeReflection.register(org.infinispan.protostream.impl.Log.class);
        RuntimeReflection.register(org.infinispan.protostream.impl.Log.class.getConstructors());
        RuntimeReflection.register(org.infinispan.protostream.impl.Log_$logger.class);
        RuntimeReflection.register(org.infinispan.protostream.impl.Log_$logger.class.getConstructors());

        try
        {
            RuntimeReflection.register(Class.forName("java.util.Arrays$ArrayList"));
            RuntimeReflection.register(Class.forName("java.util.Collections$UnmodifiableRandomAccessList"));
            RuntimeReflection.register(Class.forName("java.util.Collections$EmptyList"));
            RuntimeReflection.register(Class.forName("java.util.Collections$EmptySet"));
            RuntimeReflection.register(Class.forName("java.util.Collections$SingletonList"));
            RuntimeReflection.register(Class.forName("java.util.Collections$SingletonSet"));
            RuntimeReflection.register(Class.forName("java.util.Collections$SynchronizedSet"));
            RuntimeReflection.register(Class.forName("java.util.Collections$UnmodifiableSet"));
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
