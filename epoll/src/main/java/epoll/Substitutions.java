package epoll;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

@TargetClass(className = "io.netty.util.internal.logging.InternalLoggerFactory")
final class Target_io_netty_util_internal_logging_InternalLoggerFactory {

    @Substitute
    private static InternalLoggerFactory newDefaultFactory(String name) {
        return JdkLoggerFactory.INSTANCE;
    }
}

@TargetClass(className = "io.netty.util.AbstractReferenceCounted")
final class Target_io_netty_util_AbstractReferenceCounted {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, name = "refCnt")
    private static long REFCNT_FIELD_OFFSET;
}

public class Substitutions {}
