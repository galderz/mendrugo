package org.acme;

import java.util.Arrays;

public class Main
{
    public static void main(String[] args)
    {
        final String arg = args[0];
        final DefaultSerializerFactory factory = new DefaultSerializerFactory();
        final Serializer alice = factory.findSerializer(arg);
        final byte[] result = alice.serialize(new Object());
        System.out.println(Arrays.toString(result));
    }
}
