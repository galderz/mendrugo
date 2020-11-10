package org.acme.getting.started.commandmode.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.graalvm.compiler.api.replacements.Fold;
import org.jboss.logmanager.Level;

@TargetClass(className = "org.jboss.logmanager.Logger")
final class Target_org_jboss_logmanager_Logger
{
    @Alias
    private Target_org_jboss_logmanager_LoggerNode loggerNode;

    // TODO should be @Fold? Don't think so cos then you'd lose the ability to switch log levels at runtime for higher values than min-level
    @Substitute
    public boolean isLoggable(java.util.logging.Level level) {
        if (LevelHolder.isMinLevelEnabled(level.intValue()))
            return loggerNode.isLoggableLevel(level.intValue());

        return false;
    }
}

@TargetClass(className = "org.jboss.logmanager.LoggerNode")
final class Target_org_jboss_logmanager_LoggerNode
{
    @Alias
    boolean isLoggableLevel(int level) {
        return false;
    }
}

//@TargetClass(className = "org.jboss.logging.Logger")
//final class Target_org_jboss_logging_Logger
//{
////    @Alias
////    private String name;
//
//    // TODO if no categories, make these methods folded
//    // if categories provided, they're not folded and controlled at runtime
//
//    // START: implementations for no configuration
//
//    @Fold
//    @Substitute
//    boolean isDebugEnabled()
//    {
//        return LevelHolder.isMinLevelEnabled(Level.DEBUG);
//    }
//
//    @Fold
//    @Substitute
//    boolean isTraceEnabled()
//    {
//        return LevelHolder.isMinLevelEnabled(Level.TRACE);
//
//    }
//
////        if (name.equals("org.acme.getting.started.commandmode")
////            || name.startsWith("org.acme.getting.started.commandmode")
////        )
////        {
////            return true;
////        }
////        else
////        {
////            return false;
////        }
//
//}

// Default implementation:
// - trace/debug enabled checks folded to false
// - info enabled checks are based on runtime config
@TargetClass(className = "org.jboss.logging.Logger")
final class Target_org_jboss_logging_Logger
{
    @Fold
    @Substitute
    boolean isDebugEnabled()
    {
        return LevelHolder.isMinLevelEnabled(Level.DEBUG);
    }

    @Fold
    @Substitute
    boolean isTraceEnabled()
    {
        return LevelHolder.isMinLevelEnabled(Level.TRACE);

    }
}

class LevelHolder
{
    static final Level MIN_LEVEL = Level.INFO;
    // static final Level MIN_LEVEL = Level.TRACE;

    static boolean isMinLevelEnabled(Level level)
    {
        return isMinLevelEnabled(level.intValue());
    }

    static boolean isMinLevelEnabled(int level)
    {
        return level >= LevelHolder.MIN_LEVEL.intValue();
    }
}

public class LoggerSubstitutions
{
}