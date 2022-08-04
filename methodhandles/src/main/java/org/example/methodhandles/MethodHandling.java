package org.example.methodhandles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class MethodHandling
{
    private static final java.lang.invoke.MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static void main(String[] args) throws Throwable
    {
        final String fieldName = args[0];

        final Vehicle vehicle = new Vehicle();
        vehicle.name = "Aston";

        final MethodHandle setter = LOOKUP.unreflectSetter(Vehicle.class.getField(fieldName));
        setter.invoke(vehicle, "Mercedes");

        System.out.println(vehicle.name);
    }
}
