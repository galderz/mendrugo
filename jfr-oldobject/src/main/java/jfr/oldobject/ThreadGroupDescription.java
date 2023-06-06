package jfr.oldobject;

public class ThreadGroupDescription
{
    static Node leak;

    public static void main(String[] args)
    {
        leak = threadGroupDescription();
        Blackhole.blackhole(leak);
    }

    private static Node threadGroupDescription()
    {
        Node node = new Node();
        for (int i = 0; i < 1_000_000; i++)
        {
            node.value = new MyThreadGroup();
            node.left = new Node();
            node.right = new Node();
            node = node.right;
        }
        return node;
    }

    public final static class MyThreadGroup extends ThreadGroup
    {
        public final static String NAME = "My Thread Group";

        public MyThreadGroup()
        {
            super(NAME);
        }
    }
}
