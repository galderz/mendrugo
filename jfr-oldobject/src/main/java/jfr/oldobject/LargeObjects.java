package jfr.oldobject;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LargeObjects
{
    public static void main(String[] args)
    {
        Map<Object, Node> leaks = new HashMap<>();
        for (int i = 0; i < 1_000_000; i++) {
            Node node = new Node();
            node.left = new Node();
            node.right = new Node();
            node.right.value = new Big();
            leaks.put(i, node);
        }

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    static class Big {
        public long value1;
        public Object value2;
        float value3;
        int value4;
        double value5;
    }

    static class Node {
        Node left;
        Node right;
        Object value;
    }
}
