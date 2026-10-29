package io.quarkus.crema;

import java.lang.reflect.Field;

import org.graalvm.nativeimage.hosted.Feature;

/**
 * Prevents non-JDK System.LoggerFinder implementations (e.g., JBossLoggerFinder)
 * from being discovered during native-image build. During build, features like
 * TrustStoreManagerFeature trigger JDK internal logging which discovers the
 * LoggerFinder via SPI. If jboss-logmanager is on the classpath, its
 * JBossLoggerFinder gets cached and leaks into the image heap.
 *
 * This feature runs in afterRegistration (same phase as TrustStoreManagerFeature)
 * and in beforeAnalysis to ensure the cached finder is reset to null. The finder
 * will be re-discovered via ServiceLoader at runtime.
 */
public class ResetLoggerFinderFeature implements Feature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        resetLoggerFinder();
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Reset again in case another feature's afterRegistration triggered
        // the LoggerFinder SPI after our afterRegistration ran
        resetLoggerFinder();
    }

    @Override
    public void afterAnalysis(AfterAnalysisAccess access) {
        // Final reset to ensure nothing leaked
        resetLoggerFinder();
    }

    private static void resetLoggerFinder() {
        resetField(System.LoggerFinder.class, "service");
        try {
            resetField(Class.forName("jdk.internal.logger.LoggerFinderLoader"), "service");
        } catch (ClassNotFoundException e) {
            // JDK version without LoggerFinderLoader
        }
        // Also reset cached loggers in JDK internals that may hold JBoss logger wrappers
        try {
            resetField(Class.forName("jdk.internal.event.EventHelper"), "securityLogger");
        } catch (ClassNotFoundException e) {
            // JDK version without EventHelper
        }
    }

    private static void resetField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object current = field.get(null);
            if (current != null) {
                String className = current.getClass().getName();
                if (!className.startsWith("jdk.internal.") && !className.startsWith("java.") && !className.startsWith("sun.")) {
                    field.set(null, null);
                }
            }
        } catch (ReflectiveOperationException e) {
            // If we can't reset, continue
        }
    }

    @Override
    public String getDescription() {
        return "Resets System.LoggerFinder to prevent non-JDK finders from leaking into the image heap";
    }
}
