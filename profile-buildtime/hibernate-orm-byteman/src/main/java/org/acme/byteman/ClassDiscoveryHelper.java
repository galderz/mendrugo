package org.acme.byteman;

import com.oracle.graal.pointsto.ObjectScanner;
import com.oracle.graal.pointsto.ObjectScanner.ScanReason;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClassDiscoveryHelper extends Helper
{
    static final Map<Class<?>, Reason> DISCOVERY_REASONS = new ConcurrentHashMap<>();

    static final Map<Thread, Object> CLASS_ORIGINS = new ConcurrentHashMap<>();

    protected ClassDiscoveryHelper(Rule rule)
    {
        super(rule);
    }

    public void trackRegisterTypeForClass(Object origin)
    {
        // TODO track class origins independently,
        //      if not found in the discovery reasons,
        //      they could have been added as a result of adding a image heap object
        //      then, use the image heap object and its reason to build a chain of reasons (recursively go through previous)
        CLASS_ORIGINS.put(Thread.currentThread(), origin);
    }

    public void trackImageHeapObject(String value, ScanReason scanReason)
    {
        // System.out.println(" create image object " + value + " because " + scanReason.toString());
    }

    public void trackEmbeddedRootScanPosition(ScanReason scanReason, Object position, Object caller, Object callerCaller)
    {
        System.out.println("create embedded root scan " + scanReason + "reason\n" + position + "\n" + caller + "\n" + callerCaller);
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
        else if (stack.contains("ReflectionDataBuilder.registerTypesForClass"))
        {
            // TODO a class can be a superclass or interface for multiple, e.g.
            //      org.hibernate.engine.jdbc.AbstractLobCreator is superclass for org.hibernate.engine.jdbc.ContextualLobCreator and org.hibernate.engine.jdbc.NonContextualLobCreator
            DISCOVERY_REASONS.putIfAbsent(clazz, new Reason(numIteration, clazz
                , "Because it's a superclass or interface for %s".formatted(CLASS_ORIGINS.get(Thread.currentThread()))
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
        entries.sort(Comparator.comparing(e -> Objects.nonNull(e.getKey().getCanonicalName()) ? e.getKey().getCanonicalName() : e.getKey().getName()));

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
