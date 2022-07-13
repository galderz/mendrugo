package org.acme.byteman;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReflectionRegistryHelper extends Helper
{
    static final Map<Class<?>, Reason> REFLECTION_REASONS = new ConcurrentHashMap<>();

    static Object CONFIGURATION_LOCATION;
    static String BUNDLE_NAME;

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

    public void trackReflectionRegistration(int numIteration, Object obj)
    {
        Class<?> clazz = (Class<?>) obj;

        if (REFLECTION_REASONS.containsKey(clazz))
            return;

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
        else if (stack.contains("com.oracle.svm.reflect.hosted.ReflectionFeature.beforeAnalysis"))
        {
            REFLECTION_REASONS.put(clazz, new Reason(numIteration, clazz
                , "Because GraalVM registers it for Object[]"
            ));
        }
        else
        {
            System.out.printf("Unknown reflection registration of %s at %d iteration:%n", clazz, numIteration);
            System.out.println(formatStack());
        }
    }

    public void printSummary() throws NoSuchAlgorithmException
    {
        final List<Map.Entry<Class<?>, Reason>> entries = new ArrayList<>(REFLECTION_REASONS.entrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().toString()));

        final String summary = entries.stream()
            .map(e -> "%d,%s,%s".formatted(e.getValue().iteration, e.getKey().getCanonicalName(), e.getValue().reason))
            .collect(Collectors.joining("\n"));

        MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] digest = md.digest(summary.getBytes(StandardCharsets.UTF_8));

        System.out.printf("BEGIN %x%n", new BigInteger(1, digest));
        System.out.println(summary);
        System.out.printf("END %x%n", new BigInteger(1, digest));
    }

    record Reason(int iteration, Class<?> clazz, String reason) {}
}
