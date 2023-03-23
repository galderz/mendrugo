package jfr.oldobject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ArrayLeak
{
    static Object leak;

    public static void main(String[] args)
    {
        Object[] node = new Object[3];
        leak = node;
        for (int i = 0; i < 1_000_000; i++)
        {
            Object[] value = new Object[100];
            node[0] = value;
            Object[] left = new Object[3];
            node[1] = left;
            Object[] right = new Object[3];
            node[2] = right;
            node = right;
        }

        Blackhole.blackhole(leak);
    }
}
