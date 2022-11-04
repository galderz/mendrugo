package org.acme;

public class SecurityDemo
{
    public static String show()
    {
        return show(BaseComponent.class); // exported from module
    }

    private static String show(Class<?> intf) {
        return Proxies.simpleProxy(
            intf, null).getClass().toString();
    }
}
