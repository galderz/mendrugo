package org.acme;

public class SecurityDemo
{
    public static String show()
    {
        return show(BaseComponent.class).getImageData().toString();
    }

    private static <T> T show(Class<T> intf) {
        return Proxies.simpleProxy(
            intf, null);
    }
}
