package mendrugo.infinispan.embedded;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.manager.DefaultCacheManager;

import java.io.IOException;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class EmbeddedStress
{
    static final int NUM_ENTRIES = Integer.getInteger("num.entries", 500_000);
    static final int NUM_CHARS = 2_000;
    static final Random R = new Random(System.currentTimeMillis());

    public static void main(String[] args) throws IOException
    {
        try (DefaultCacheManager infinispan = getInfinispan())
        {
            final var cache = infinispan.getCache("local");
            IntStream.range(1, NUM_ENTRIES).forEach(onCache(cache));
        }
    }

    private static IntConsumer onCache(Cache<Object, Object> cache)
    {
        return i ->
        {
            if (i % 10000 == 0)
            {
                System.out.printf("Stored %d entries%n", i);
            }

            final var key = String.valueOf(i);
            final var value = generateRandomString(NUM_CHARS);
            cache.put(key, value);
        };
    }

    public static String generateRandomString(int numberOfChars)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfChars; i++)
            sb.append((char) (64 + R.nextInt(26)));
        return sb.toString();
    }

    static DefaultCacheManager getInfinispan()
    {
        DefaultCacheManager cacheManager = new DefaultCacheManager();

        final var builder = new ConfigurationBuilder();
        builder.memory().storage(StorageType.OFF_HEAP);
        cacheManager.defineConfiguration("local", builder.build());

        return cacheManager;
    }
}
