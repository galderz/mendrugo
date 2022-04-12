package org.example.anon.java.io;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ObjectStreamClass
{
    private final Class<?> cl;

    private ObjectStreamClass(final Class<?> cl)
    {
        this.cl = cl;
    }

    public static class Caches
    {
        public static final ConcurrentMap<WeakClassKey, Reference<?>> localDescs =
            new ConcurrentHashMap<>();

        /**
         * cache mapping field group/local desc pairs -> field reflectors
         */
        public static final ConcurrentMap<FieldReflectorKey, Reference<?>> reflectors =
            new ConcurrentHashMap<>();

        static
        {
            System.out.println(
                "[ObjectStreamClass.Caches] Local descs is " + System.identityHashCode(localDescs))
            ;
            System.out.println(
                "[ObjectStreamClass.Caches] Reflectors is " + System.identityHashCode(reflectors)
            );
        }
    }

    private static class WeakClassKey {}

    private static class FieldReflectorKey {}

    private static class FieldReflector {}
}
