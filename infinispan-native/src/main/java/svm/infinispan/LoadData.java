package svm.infinispan;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.configuration.XMLStringConfiguration;

import java.util.Random;

public class LoadData
{
    static final Random R = new Random(System.currentTimeMillis());
    static final int NUM_ENTRIES = Integer.getInteger("num.entries", 500_000);
    static final int NUM_CHARS = 2_000;

    public static void main(String[] args)
    {
        // Create a configuration for a locally-running server
        var builder = new ConfigurationBuilder();
        builder.addServer()
            .host("127.0.0.1")
            .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
            .security().authentication()
                // Add user credentials.
                .username("foo")
                .password("bar")
                .realm("default")
                .saslMechanism("DIGEST-MD5");

        String cacheName = "test";

        String xml = String.format(
            "<infinispan>" +
                "<cache-container>" +
                    "<distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\">" +
                        "<memory storage=\"OFF_HEAP\"/>" +
                        "<state-transfer enabled=\"true\" timeout=\"120000\" await-initial-transfer=\"true\"/>" +
                    "</distributed-cache>" +
                "</cache-container>" +
            "</infinispan>"
            , cacheName);

        try (RemoteCacheManager remote = new RemoteCacheManager(builder.build()))
        {
            final var cache = remote.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache("test", new XMLStringConfiguration(xml));

            for (int i = 0; i < NUM_ENTRIES; i++)
            {
                if (i % 10000 == 0)
                {
                    System.out.printf("Stored %d entries%n", i);
                }

                final var key = String.valueOf(i);
                final var value = generateRandomString(NUM_CHARS);
                cache.put(key, value);
            }
        }
    }

    public static String generateRandomString(int numberOfChars)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfChars; i++)
            sb.append((char) (64 + R.nextInt(26)));
        return sb.toString();
    }
}
