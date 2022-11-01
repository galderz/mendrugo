package jfr.oldobject;

import jfr.oldobject.gen.Huge;

import java.util.Scanner;

public class HugeObject
{
    static Huge leak;

    public static void main(String[] args)
    {
        leak = new Huge();

        System.out.println("Press enter to finish program");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // Use the leak, to avoid being dead code eliminated
        if (leak.hashCode() == System.nanoTime())
        {
            System.out.println(" ");
        }
    }
}
