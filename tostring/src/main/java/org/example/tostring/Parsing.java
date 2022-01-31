package org.example.tostring;

import org.example.optional.NumberParser;

public class Parsing implements Runnable
{
    @Override
    public void run()
    {
        System.out.write(new byte[]{48, 49, 50}, 0, 3);
    }

    @Override
    public String toString()
    {
        // return "123";
        return Integer.toString(number());
    }

    public int number()
    {
        return new NumberParser().parseInt("123");
    }
}
