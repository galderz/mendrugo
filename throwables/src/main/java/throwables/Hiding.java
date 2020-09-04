package throwables;

import java.util.Arrays;

public class Hiding
{
    public static void main(String[] args)
    {
        System.out.println("--- new Throwable()");
        try
        {
            throw new Throwable();
        }
        catch (Throwable t)
        {
            useThrowable(t);
        }
        System.out.println("---");

        System.out.println("--- new Throwable(String)");
        try
        {
            throw new Throwable("Caught you!");
        }
        catch (Throwable t)
        {
        }
        System.out.println("---");

        System.out.println("--- new Exception()");
        try
        {
            throw new Exception();
        }
        catch (Throwable t)
        {
        }
        System.out.println("---");
    }

    private static void useThrowable(Throwable t)
    {
        try
        {
            final StackTraceElement[] st = t.getStackTrace();
            final var stackMinusOne = Arrays.copyOfRange(st, 1, st.length);
            if (stackMinusOne.length > 10000000)
                System.out.println("");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
