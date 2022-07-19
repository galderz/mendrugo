package org.acme.byteman;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

public class ClassDiscoveryHelper extends Helper
{
    static final Map<Class<?>, Reason> DISCOVERY_REASONS = new ConcurrentHashMap<>();

    protected ClassDiscoveryHelper(Rule rule)
    {
        super(rule);
    }

    public void trackClassDiscovery(int numIteration, Class<?> clazz)
    {
        final String stack = formatStack();
        if (stack.contains("ReflectionDataBuilder.processRegisteredElements"))
        {
            DISCOVERY_REASONS.putIfAbsent(clazz, new Reason(numIteration, clazz
                , "Because it was registered for reflection"
            ));
        }
        else
        {
            System.out.printf("Unknown class discovery of %s at %d iteration:%n%s%n", clazz, numIteration, formatStack());
        }
    }

    public void printSummary() throws NoSuchAlgorithmException, IOException
    {
        final List<Map.Entry<Class<?>, Reason>> entries = new ArrayList<>(DISCOVERY_REASONS.entrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().getCanonicalName()));

        final String summary = entries.stream()
            .map(e -> "%d,%s,%s".formatted(e.getValue().iteration(), e.getKey().getCanonicalName(), e.getValue().reason()))
            .collect(Collectors.joining("\n"));

        MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] digest = md.digest(summary.getBytes(StandardCharsets.UTF_8));

        final String header = "BEGIN %x".formatted(new BigInteger(1, digest));
        final String footer = "END %x".formatted(new BigInteger(1, digest));
        System.out.println(header);
        System.out.println(summary);
        System.out.println(footer);

        PrintWriter printWriter = new PrintWriter(new FileWriter("class-discovery.csv"));
        printWriter.println(header);
        printWriter.println(summary);
        printWriter.println(footer);
        printWriter.close();
    }
}
