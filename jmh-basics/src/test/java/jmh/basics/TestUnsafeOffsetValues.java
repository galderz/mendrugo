package jmh.basics;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class TestUnsafeOffsetValues
{
    private static final Unsafe U;

    static {
        try {
            Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            U = (Unsafe) unsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException
    {
        System.out.printf(
            "IterationParamsL4: off = %d, markerBegin = %d, markerEnd = %d"
            , getOffset(Class.forName("org.openjdk.jmh.infra.IterationParamsL4"), "type")
            , getOffset(Class.forName("org.openjdk.jmh.infra.IterationParamsL4"), "markerBegin")
            , getOffset(Class.forName("org.openjdk.jmh.infra.IterationParamsL4"), "markerEnd")
        );
    }

    public static long getOffset(Class<?> klass, String fieldName) {
        do {
            try {
                Field f = klass.getDeclaredField(fieldName);
                return U.objectFieldOffset(f);
            } catch (NoSuchFieldException e) {
                // whatever, will try superclass
            }
            klass = klass.getSuperclass();
        } while (klass != null);
        throw new IllegalStateException("Can't find field \"" + fieldName + "\"");
    }

}
