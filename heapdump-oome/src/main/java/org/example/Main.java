package org.example;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        final List<byte[]> leak = new ArrayList<>();
        int index = 1;

        while (true) {
            byte[] b = new byte[10 * 1024 * 1024]; // 10MB byte object
            leak.add(b);
            Runtime rt = Runtime.getRuntime();
            System.out.printf("[%3s] Available heap memory: %s%n", index++, rt.freeMemory());
        }
    }
}