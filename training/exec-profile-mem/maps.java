///usr/bin/env jbang "$0" "$@" ; exit $?
// //DEPS <dependency1> <dependency2>

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.*;

/**
 * Adapted from Andrei Pangin's examples:
 * https://github.com/apangin/java-profiling-presentation/blob/master/src/demo7/MapGenerator.java
 * https://github.com/apangin/java-profiling-presentation/blob/master/src/demo7/MapReader.java
 */
public class maps
{
    public static void main(String[] args) throws Exception
    {
        if (args.length > 0 && "gen".equals(args[0]))
        {
            generateMap();
        }
        else
        {
            readMap();
        }
    }

    private static void readMap() throws Exception
    {
        long startTime = System.nanoTime();
        Map<String, Long> map = readMap("in.txt");
        long time = System.nanoTime() - startTime;

        System.out.printf("Read %d elements in %.3f seconds\n",
            map.size(), time / 1e9);
    }

    private static Map<String, Long> readMap(String fileName) throws IOException
    {
        Map<String, Long> map = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] kv = line.split(":", 2);
                String key = kv[0].trim();
                String value = kv[1].trim();
                map.put(key, Long.parseLong(value));
            }
        }

        return map;
    }

    private static void generateMap() throws Exception
    {
        try (PrintStream out = new PrintStream("in.txt"))
        {
            for (int i = 0; i < 10_000_000; i++)
            {
                int length = ThreadLocalRandom.current().nextInt(1, 9);
                byte[] b = new byte[length];
                ThreadLocalRandom.current().nextBytes(b);
                String key = Base64.getEncoder().encodeToString(b);
                long value = ThreadLocalRandom.current().nextLong(1000000);
                out.println(key + ": " + value);
            }
        }
    }
}