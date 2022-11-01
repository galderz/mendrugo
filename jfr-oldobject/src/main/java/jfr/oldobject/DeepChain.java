package jfr.oldobject;

import java.util.Scanner;

public class DeepChain
{
    static Object leak;

    public static void main(String[] args)
    {
        Node node = new Node();
        leak = node;
        for (int i = 0; i < 1_000_000; i++) {
            node.value = new Big();
            node.left = new Node();
            node.right = new Node();
            node = node.right;
        }

        System.out.println("Press enter to finish program");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // Use the leak, to avoid being dead code eliminated
        if (leak.hashCode() == System.nanoTime())
        {
            System.out.println(" ");
        }
    }

    static class Node {
        Node left;
        Node right;
        Object value;
    }

    static class Big {
        public long value1;
        public Object value2;
        float value3;
        int value4;
        double value5;
    }
}
