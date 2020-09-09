package recompute.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

import java.nio.ByteBuffer;

@TargetClass(className = "recompute.RecomputeAlias")
final class Target_recompute_RecomputeAlias {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    private static ByteBuffer BUFFER = ByteBuffer.allocateDirect(0);

}

@TargetClass(className = "recompute.RecomputeReset")
final class Target_recompute_RecomputeReset {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset)
    private static ByteBuffer BUFFER;

}

public class Substitutions
{
}
