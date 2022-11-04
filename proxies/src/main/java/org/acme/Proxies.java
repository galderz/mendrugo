package org.acme;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class Proxies
{
    public static <S> S simpleProxy(
        Class<? super S> subjectInterface, S subject) {
        return castProxy(subjectInterface,
            (InvocationHandler & Serializable)
                (proxy, method, args) -> method.invoke(subject, args)
        );
    }

    @SuppressWarnings("unchecked")
    public static <S> S castProxy(Class<? super S> intf,
                                  InvocationHandler handler) {
        Objects.requireNonNull(intf, "intf==null");
        Objects.requireNonNull(handler, "handler==null");
        return MethodTurboBooster.boost(
            (S) Proxy.newProxyInstance(
                intf.getClassLoader(),
                new Class<?>[] {intf},
                new ExceptionUnwrappingInvocationHandler(handler)));
    }
}
