package org.acme.getting.started.commandmode.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.graalvm.compiler.api.replacements.Fold;

@TargetClass(className = "org.jboss.logmanager.Logger")
final class Target_org_jboss_logmanager_Logger
{
    @Alias
    private Target_org_jboss_logmanager_LoggerNode loggerNode;

    // TODO should be @Fold? Don't think so cos then you'd lose the ability to switch log levels at runtime for higher values than min-level
    @Substitute
    public boolean isLoggable(java.util.logging.Level level) {
        if (level.intValue() >= org.jboss.logmanager.Level.INFO.intValue())
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

//@TargetClass(className = "org.jboss.logging.JBossLogManagerLogger")
//final class Target_org_jboss_logging_JBossLogManagerLogger
//{
//    @Alias
//    private org.jboss.logmanager.Logger logger;
//
////    // TODO should be @Fold?
////    @Substitute
////    public boolean isEnabled(final Logger.Level level)
////    {
////        if (level.ordinal() <= Logger.Level.INFO.ordinal())
////        {
////            return logger.isLoggable(translate(level));
////        }
////
////        return false;
////    }
////
////    @Alias
////    private static java.util.logging.Level translate(final Logger.Level level)
////    {
////        return null;
////    }
////
////    @Substitute
////    private void doLog(final Logger.Level level, final String loggerClassName, final Object message, final Object[] parameters, final Throwable thrown)
////    {
////        if (isEnabled(level))
////        {
////            if (parameters == null)
////            {
////                logger.log(loggerClassName, translatedLevel, String.valueOf(message), thrown);
////            }
////            else
////            {
////                logger.log(loggerClassName, translatedLevel, String.valueOf(message), ExtLogRecord.FormatStyle.MESSAGE_FORMAT, parameters, thrown);
////            }
////        }
////    }
//}

// Assume min-level=INFO (default)
@TargetClass(className = "org.jboss.logging.Logger")
final class Target_org_jboss_logging_Logger
{
//    @Alias
//    private String name;

    @Fold
    @Substitute
    boolean isDebugEnabled()
    {
        return false;
    }

    @Fold
    @Substitute
    boolean isTraceEnabled()
    {
        return false;

//        if (name.equals("org.acme.getting.started.commandmode")
//            || name.startsWith("org.acme.getting.started.commandmode")
//        )
//        {
//            return true;
//        }
//        else
//        {
//            return false;
//        }
    }
}

public class LoggerSubstitutions
{
}