package org.example.anon.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaField;
import org.example.anon.java.io.NewObjectStreamClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.oracle.svm.core.annotate.RecomputeFieldValue.Kind.Custom;
import static com.oracle.svm.core.annotate.RecomputeFieldValue.Kind.NewInstance;

@TargetClass(value = org.example.anon.java.io.NewObjectStreamClass.class, innerClass = "Caches")
final class Target_org_example_anon_java_io_NewObjectStreamClass_Caches
{
    @Alias
    @RecomputeFieldValue(
        kind = Custom
        , declClass = ClassCacheComputer.class
    )
    static Target_org_example_anon_java_io_ClassCache localDescs;

    @Alias
    @RecomputeFieldValue(
        kind = Custom
        , declClass = ClassCacheComputer.class
    )
    static Target_org_example_anon_java_io_ClassCache reflectors;
}

@TargetClass(className = "org.example.anon.java.io.ClassCache")
final class Target_org_example_anon_java_io_ClassCache {
}

final class ClassCacheComputer implements RecomputeFieldValue.CustomFieldValueTransformer
{
    @Override
    public RecomputeFieldValue.ValueAvailability valueAvailability() {
        return RecomputeFieldValue.ValueAvailability.BeforeAnalysis;
    }

    @Override
    public Object transform(
        MetaAccessProvider metaAccess
        , ResolvedJavaField original
        , ResolvedJavaField annotated
        , Object receiver
        , Object originalValue
    )
    {
        final Class<?> clazz = originalValue.getClass();
        try
        {
            final Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
