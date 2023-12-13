package org.acme;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class DefaultSerializerFactory
{
    private final static String ALICE_SERIALIZER = "org.acme.AliceSerializer";
    private final static String BOB_SERIALIZER = "org.acme.BobSerializer";

    public Serializer findSerializer(String name)
    {
        if ("Alice".equals(name)) {
            return instantiate(ALICE_SERIALIZER);
        }

        if ("Bob".equals(name)) {
            return instantiate(BOB_SERIALIZER);
        }

        return null;
    }

    private Serializer instantiate(String fqcn)
    {
        try
        {
            final Class<?> clazz = Class.forName(fqcn);
            final Constructor<?> ctor = clazz.getDeclaredConstructor();
            if (!Modifier.isPublic(ctor.getModifiers())) {
                throw new IllegalArgumentException("Default constructor is not accessible");
            }

            return (Serializer) ctor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
