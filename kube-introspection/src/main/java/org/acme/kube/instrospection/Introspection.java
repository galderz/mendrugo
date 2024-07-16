package org.acme.kube.instrospection;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.lang.reflect.Field;
import java.util.Arrays;

@QuarkusMain
public class Introspection implements QuarkusApplication
{
    @Override
    public int run(String... args) throws Exception
    {
        final Class<?> clazz = Class.forName(args[0]);
        final Field[] fields = clazz.getDeclaredFields();
        System.out.println("Fields size: " + fields.length);
        System.out.println("Fields: " + Arrays.toString(fields));
        return 0;
    }
}
