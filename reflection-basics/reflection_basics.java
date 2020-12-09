import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.function.UnaryOperator;

import static java.security.AccessController.doPrivileged;

class reflection_basics
{
    public static void main(String[] args) throws Exception
    {
        needEnabledAsserts();
        invokeReflectMethodViaBakedClass();
        invokeReflectMethodViaParamClass(ArrayList.class);
        invokePrivilegedReflectMethodViaParamClass(ArrayList.class);
        lazyInvokePrivilegedReflectMethodViaParamClass(ArrayList.class, null);
        System.out.println("Assertions passed");
    }

    private static void lazyInvokePrivilegedReflectMethodViaParamClass(final Class<?> clazz, final Class<?> returnType) throws Exception
    {
        UnaryOperator<Object> lazy = lazyReflectClone(clazz);

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        assert list.equals(list.clone());
        assert list.equals(lazy.apply(list));
    }

    private static UnaryOperator<Object> lazyReflectClone(Class<?> clazz)
    {
        final var method = doPrivileged((PrivilegedAction<Method>) () ->
        {
            try
            {
                return clazz.getMethod("clone");
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        });

        return obj ->
        {
            try
            {
                return method.invoke(obj);
            }
            catch (RuntimeException | Error e)
            {
                throw e;
            }
            catch (Throwable throwable)
            {
                throw new UndeclaredThrowableException(throwable);
            }
        };
    }

    private static void invokePrivilegedReflectMethodViaParamClass(Class<?> clazz) throws Exception
    {
        final var method = doPrivileged((PrivilegedAction<Method>) () ->
        {
            try
            {
                return clazz.getMethod("clone");
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        });

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        assert list.equals(list.clone());
        assert list.equals(method.invoke(list));
    }

    private static void invokeReflectMethodViaParamClass(Class<?> clazz) throws Exception
    {
        final var method = clazz.getMethod("clone");

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        assert list.equals(list.clone());
        assert list.equals(method.invoke(list));
    }

    private static void invokeReflectMethodViaBakedClass() throws Exception
    {
        final var method = ArrayList.class.getMethod("clone");

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        assert list.equals(list.clone());
        assert list.equals(method.invoke(list));
    }

    private static void needEnabledAsserts()
    {
        boolean enabled = false;
        //noinspection AssertWithSideEffects
        assert enabled = true;
        //noinspection ConstantConditions
        if (!enabled)
        {
            throw new AssertionError("assert not enabled");
        }
    }
}