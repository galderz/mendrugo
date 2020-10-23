package org.acme.getting.started.commandmode.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.graalvm.compiler.api.replacements.Fold;

@TargetClass(className = "org.jboss.logging.Logger")
final class Target_org_jboss_logging_Logger
{
    @Alias
    private String name;

    @Fold
    @Substitute
    boolean isTraceEnabled()
    {
        if (name.equals("org.acme.getting.started.commandmode")
            || name.startsWith("org.acme.getting.started.commandmode")
        )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

public class LoggerSubstitutions
{
}
