package org.example.runnable;

public class Main
{
    static
    {
        System.out.println("Main static block");
    }

    public static void main(String[] args) throws InterruptedException
    {
        System.out.println("Hello, World!");
        final Thread t = new Thread(new UsedRunnable());
        t.start();
        t.join();
    }
}
