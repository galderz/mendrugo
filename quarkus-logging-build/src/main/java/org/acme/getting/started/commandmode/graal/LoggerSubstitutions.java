package org.acme.getting.started.commandmode.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.InjectAccessors;
import com.oracle.svm.core.annotate.TargetClass;
import org.acme.getting.started.commandmode.GreetingLogger;
import org.jboss.logging.Logger;

@TargetClass(value = GreetingLogger.class)
final class Target_org_acme_getting_started_commandmode_GreetingLogger
{
    @Alias
    @InjectAccessors(IsTraceAccessor.class)
    static boolean isTrace;
}

final class IsTraceAccessor
{
    static boolean getIsTrace()
    {
        return RuntimeIsTraceAccessor.isTrace;
    }
}

final class RuntimeIsTraceAccessor
{
    static final Logger LOG = Logger.getLogger(GreetingLogger.class);
    static final boolean isTrace = LOG.isTraceEnabled();
}

public class LoggerSubstitutions
{
}
