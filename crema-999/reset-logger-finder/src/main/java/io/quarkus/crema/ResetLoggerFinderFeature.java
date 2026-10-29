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
        // Pre-set the LoggerFinder to the JDK default BEFORE any other feature
        // triggers the SPI lookup. This way JBossLoggerFinder is never discovered.
        installJdkDefaultLoggerFinder();
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Ensure it's still the JDK default
        installJdkDefaultLoggerFinder();
    }

    @Override
    public void afterAnalysis(AfterAnalysisAccess access) {
        // Final cleanup
        installJdkDefaultLoggerFinder();
    }

    /**
     * Force the JDK default LoggerFinder into the cache, preventing SPI discovery
     * of non-JDK finders like JBossLoggerFinder.
     */
    private static void installJdkDefaultLoggerFinder() {
        try {
            // Create the JDK default LoggerFinder directly
            Class<?> defaultFinderClass = Class.forName("jdk.internal.logger.DefaultLoggerFinder");
            java.lang.reflect.Constructor<?> ctor = defaultFinderClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            System.LoggerFinder defaultFinder = (System.LoggerFinder) ctor.newInstance();

            // Set it in both caches to prevent SPI lookup
            setField(System.LoggerFinder.class, "service", defaultFinder);
            try {
                setField(Class.forName("jdk.internal.logger.LoggerFinderLoader"), "service", defaultFinder);
            } catch (ClassNotFoundException e) {
                // OK
            }

            // Also set the LazyLoggers provider
            try {
                setField(Class.forName("jdk.internal.logger.LazyLoggers"), "provider", defaultFinder);
            } catch (ClassNotFoundException e) {
                // OK
            }

            // Reset any cached loggers that might hold JBoss wrappers
            try {
                setField(Class.forName("jdk.internal.event.EventHelper"), "securityLogger", null);
            } catch (ClassNotFoundException e) {
                // OK
            }

            // Also reset the java.util.logging.LogManager singleton
            // The JBoss LogManager is registered via META-INF/services and gets
            // instantiated by LogManager.<clinit>. We need to replace it with
            // the JDK default.
            try {
                // Reset the manager field to force re-init at runtime
                setField(java.util.logging.LogManager.class, "manager", null);
            } catch (ReflectiveOperationException e2) {
                // OK
            }
        } catch (ReflectiveOperationException e) {
            // If we can't install default, try the reset approach as fallback
            System.err.println("ResetLoggerFinderFeature: failed to install default finder: " + e);
        }
    }

    private static void setField(Class<?> clazz, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static void resetField(Class<?> clazz, String fieldName) {
        resetField(clazz, fieldName, false);
    }

    /**
     * @param always if true, reset regardless of the field value's class origin
     */
    private static void resetField(Class<?> clazz, String fieldName, boolean always) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object current = field.get(null);
            if (current != null) {
                String className = current.getClass().getName();
                System.err.println("ResetLoggerFinderFeature: " + clazz.getName() + "." + fieldName + " = " + className);
                if (always || (!className.startsWith("jdk.internal.") && !className.startsWith("java.") && !className.startsWith("sun."))) {
                    field.set(null, null);
                    System.err.println("ResetLoggerFinderFeature: RESET " + clazz.getName() + "." + fieldName + " to null");
                }
            } else {
                System.err.println("ResetLoggerFinderFeature: " + clazz.getName() + "." + fieldName + " = null (nothing to reset)");
            }
        } catch (ReflectiveOperationException e) {
            System.err.println("ResetLoggerFinderFeature: failed to reset " + clazz.getName() + "." + fieldName + ": " + e);
        }
    }

    @Override
    public String getDescription() {
        return "Resets System.LoggerFinder to prevent non-JDK finders from leaking into the image heap";
    }
}
