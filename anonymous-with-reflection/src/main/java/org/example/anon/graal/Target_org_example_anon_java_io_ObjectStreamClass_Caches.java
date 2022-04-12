package org.example.anon.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.oracle.svm.core.annotate.RecomputeFieldValue.Kind.NewInstance;

@TargetClass(value = org.example.anon.java.io.ObjectStreamClass.class, innerClass = "Caches")
final class Target_org_example_anon_java_io_ObjectStreamClass_Caches {

    @Alias
    @RecomputeFieldValue(kind = NewInstance, declClass = ConcurrentHashMap.class)
    static ConcurrentMap<?, ?> localDescs;

    @Alias
    @RecomputeFieldValue(kind = NewInstance, declClass = ConcurrentHashMap.class)
    static ConcurrentMap<?, ?> reflectors;
}
