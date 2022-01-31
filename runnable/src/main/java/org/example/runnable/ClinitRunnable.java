package org.example.runnable;

public class ClinitRunnable implements Runnable
{
    static
    {
        System.out.println("Class init block");
    }

    @Override
    public void run()
    {
        System.out.println("Clinit.run()");
    }
}
