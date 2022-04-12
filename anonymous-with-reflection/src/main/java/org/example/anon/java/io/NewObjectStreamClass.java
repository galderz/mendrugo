package org.example.anon.java.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewObjectStreamClass
{
    private final Class<?> cl;

    private NewObjectStreamClass(final Class<?> cl)
    {
        this.cl = cl;
    }

    public static class Caches
    {
        /** cache mapping local classes -> descriptors */
        public static final ClassCache<NewObjectStreamClass> localDescs =
            new ClassCache<>() {
                @Override
                protected NewObjectStreamClass computeValue(Class<?> type) {
                    return new NewObjectStreamClass(type);
                }
            };

        /** cache mapping field group/local desc pairs -> field reflectors */
        public static final ClassCache<Map<FieldReflectorKey, FieldReflector>> reflectors =
            new ClassCache<>() {
                @Override
                protected Map<FieldReflectorKey, FieldReflector> computeValue(Class<?> type) {
                    return new ConcurrentHashMap<>();
                }
            };

        static
        {
            System.out.println(
                "[NewObjectStreamClass.Caches] Local descs is " + System.identityHashCode(localDescs))
            ;
            System.out.println(
                "[NewObjectStreamClass.Caches] Reflectors is " + System.identityHashCode(reflectors)
            );
        }
    }

    private static class WeakClassKey {}

    private static class FieldReflectorKey {}

    private static class FieldReflector {}
}
