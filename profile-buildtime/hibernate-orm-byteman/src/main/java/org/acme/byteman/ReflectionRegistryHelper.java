package org.acme.byteman;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReflectionRegistryHelper extends Helper
{
    static final Map<Class<?>, Reason> REFLECTION_REASONS = new HashMap<>();

    static Object CONFIGURATION_LOCATION;
    static String BUNDLE_NAME;
    static Locale BUNDLE_LOCALE;

    protected ReflectionRegistryHelper(Rule rule)
    {
        super(rule);
    }

    public void trackConfigurationParsing(Object location)
    {
        CONFIGURATION_LOCATION = location;
    }

    public void trackBundle(String bundleName)
    {
        BUNDLE_NAME = bundleName;
    }

    public void trackReflectionRegistration(int numIteration, Class<?> clazz)
    {
        final String stack = formatStack();
        if (stack.contains("org.hibernate.graalvm.internal.GraalVMStaticAutofeature"))
        {
            REFLECTION_REASONS.put(clazz, new Reason(numIteration, clazz
                , "Because Hibernate registered statically"
            ));
        }
        else if (stack.contains("com.oracle.svm.hosted.config.ConfigurationParserUtils"))
        {
            REFLECTION_REASONS.put(clazz, new Reason(numIteration, clazz
                , "Because it was defined in configuration file %s".formatted(CONFIGURATION_LOCATION.toString())
            ));
        }
        else if (stack.contains("com.oracle.svm.core.jdk.localization.LocalizationSupport"))
        {
            REFLECTION_REASONS.put(clazz, new Reason(numIteration, clazz
                , "Because of bundle %s".formatted(BUNDLE_NAME)
            ));
        }
        else if (stack.contains("io.quarkus.runner.AutoFeature"))
        {
            REFLECTION_REASONS.put(clazz, new Reason(numIteration, clazz
                , "Because Quarkus registers it directly or via an extension"
            ));
        }
        else if (stack.contains("com.oracle.svm.hosted.jdk.JNIRegistrationJavaNio"))
        {
            REFLECTION_REASONS.put(clazz, new Reason(numIteration, clazz
                , "Because GraalVM registers it for JNI access to Java NIO"
            ));
        }
        else
        {
            System.out.printf("Unknown reflection registration of %s at %d iteration:%n", clazz, numIteration);
            System.out.println(formatStack());
        }
    }

    public void printSummary()
    {
        final List<Map.Entry<Class<?>, Reason>> entries = new ArrayList<>(REFLECTION_REASONS.entrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().toString()));

        final String summary = entries.stream()
            .map(e -> "%d,%s,%s".formatted(e.getValue().iteration, e.getKey(), e.getValue().reason))
            .collect(Collectors.joining("\n"));

        System.out.println(summary);
    }

    record Reason(int iteration, Class<?> clazz, String reason) {}
}
