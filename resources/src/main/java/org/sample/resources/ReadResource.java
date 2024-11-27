package org.sample.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class ReadResource
{
    public static final String SONG_LIST = "/META-INF/SongList";

    public static void main(String[] args) throws Exception
    {
        final String resource = SONG_LIST;

        final Enumeration<URL> urls = ReadResource.class
            .getClassLoader()
            .getResources(
                resource.startsWith("/")
                    ? resource.substring(1)
                    : resource
            );

        while (urls.hasMoreElements())
        {
            final URL url = urls.nextElement();
            System.out.println("[resource-reader] Trying resource url: " + url);
            try (final BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)
            ))
            {
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
            }
        }
    }
}
