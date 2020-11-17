package org.acme.getting.started.commandmode.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.graalvm.compiler.api.replacements.Fold;
import org.jboss.logmanager.Level;

import java.util.HashMap;
import java.util.Map;

@TargetClass(className = "org.jboss.logmanager.Logger")
final class Target_org_jboss_logmanager_Logger
{
    @Alias
    private String name;

    @Alias
    private Target_org_jboss_logmanager_LoggerNode loggerNode;

    @Substitute
    public boolean isLoggable(java.util.logging.Level level) {
        if (LevelHolder.isMinLevelEnabled(level.intValue(), name))
            return loggerNode.isLoggableLevel(level.intValue());

//        if (level.intValue() >= Level.INFO.intValue())
//            return loggerNode.isLoggableLevel(level.intValue());

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

// Custom implementation:
// - user has set min level for a given category to TRACE
// - checks for isDebugEnabled and isTraceEnabled cannot be folded
@TargetClass(className = "org.jboss.logging.Logger")
final class Target_org_jboss_logging_Logger
{
    @Alias
    private String name;

//    @Fold
    @Substitute
    boolean isDebugEnabled()
    {
        return LevelHolder.isMinLevelEnabled(Level.DEBUG.intValue(), name);
    }

//    @Fold
    @Substitute
    boolean isTraceEnabled()
    {
        return LevelHolder.isMinLevelEnabled(Level.TRACE.intValue(), name);

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

//// Default implementation:
//// - trace/debug enabled checks folded to false
//// - info enabled checks are based on runtime config
//@TargetClass(className = "org.jboss.logging.Logger")
//final class Target_org_jboss_logging_Logger
//{
//    @Fold
//    @Substitute
//    boolean isInfoEnabled()
//    {
//        return true;
//    }
//
//    @Fold
//    @Substitute
//    boolean isDebugEnabled()
//    {
//        return false;
//    }
//
//    @Fold
//    @Substitute
//    boolean isTraceEnabled()
//    {
//        return false;
//    }
//}

class LevelHolder
{
    // static final Level MIN_LEVEL = Level.INFO;
    // static final Level MIN_LEVEL = Level.TRACE;
    static final Map<String, Level> MIN_LEVELS = new HashMap<>();

    static {
        MIN_LEVELS.put("org.acme.getting.started.commandmode", Level.TRACE);
    }

    static boolean isMinLevelEnabled(int level, String name)
    {
        for (Map.Entry<String, Level> entry : MIN_LEVELS.entrySet())
        {
            if (name.equals(entry.getKey())
                || name.startsWith(entry.getKey())
            )
            {
                return isMinLevelEnabled(level, entry.getValue().intValue());
            }
        }

        return false;
    }

    static boolean isMinLevelEnabled(int level, int minLevel)
    {
        return level >= minLevel;
    }
}

public class LoggerSubstitutions
{
}