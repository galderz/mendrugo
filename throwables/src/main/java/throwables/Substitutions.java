package throwables;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(Throwable.class)
final class Target_java_lang_Throwable
{
    @Substitute
    public Target_java_lang_Throwable()
    {
        StackTraces.show(null, StackTraces.getStackTrace(), this.getClass().getSimpleName());
    }

    @Substitute
    public Target_java_lang_Throwable(String message)
    {
        StackTraces.show(message, StackTraces.getStackTrace(), this.getClass().getSimpleName());
    }

    @Substitute
    public StackTraceElement[] getStackTrace()
    {
        // Not accurate but should avoid the exception
        // that stops Quarkus from starting up.
        return StackTraces.getStackTrace();
    }
}

public class Substitutions
{
}
