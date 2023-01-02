package jfr.oldobject;

public class PlainObjectLeak
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

        Blackhole.blackhole(leak);
    }

    static class Node {
        Node left;
        Node right;
        Object value;

        @Override
        public String toString()
        {
            return "Node{" +
                "left=" + left +
                ", right=" + right +
                ", value=" + value +
                '}';
        }
    }

    static class Big {
        public long value1;
        public Object value2;
        float value3;
        int value4;
        double value5;

        @Override
        public String toString()
        {
            return "Big{" +
                "value1=" + value1 +
                ", value2=" + value2 +
                ", value3=" + value3 +
                ", value4=" + value4 +
                ", value5=" + value5 +
                '}';
        }
    }
}
