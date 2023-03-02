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
}
