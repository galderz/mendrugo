package org.acme;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class MethodTurboBooster
{
    private static final Booster BOOSTER =
        Boolean.getBoolean(
            MethodTurboBooster.class.getName() + ".disabled") ?
            new BoosterOff() : new BoosterOn();

    public static <E> E boost(E proxy)
    {
        return BOOSTER.turboBoost(proxy);
    }

    public static Method boost(Method method)
    {
        return BOOSTER.turboBoost(method);
    }

    private interface Booster
    {
        <E> E turboBoost(E proxy);

        Method turboBoost(Method method);
    }

    private static class BoosterOn implements Booster
    {
        @Override
        public <E> E turboBoost(E proxy)
        {
            if (!(proxy instanceof Proxy))
                throw new IllegalArgumentException(
                    "Can only turboboost instances of Proxy"
                );
            try
            {
                for (var field : proxy.getClass().getDeclaredFields())
                {
                    if (field.getType() == Method.class &&
                        field.trySetAccessible())
                    {
                        turboBoost((Method) field.get(null));
                    }
                }
                return proxy;
            }
            catch (IllegalAccessException | SecurityException e)
            {
                // could not turbo-boost - return proxy unchanged;
                return proxy;
            }
        }

        @Override
        public Method turboBoost(Method method)
        {
            try
            {
                method.trySetAccessible();
            }
            catch (SecurityException e)
            {
                // could not turbo-boost - return method unchanged;
            }
            return method;
        }
    }

    private static class BoosterOff implements Booster
    {
        @Override
        public <E> E turboBoost(E proxy)
        {
            if (!(proxy instanceof Proxy))
                throw new IllegalArgumentException(
                    "Can only turboboost instances of Proxy"
                );
            return proxy;
        }

        @Override
        public Method turboBoost(Method method)
        {
            return method;
        }
    }
}
