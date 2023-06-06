package jfr.oldobject;

public class ObjectDescription
{
    static Node threadGroupLeak;
    static Node threadLeak;

    public static void main(String[] args)
    {
        threadGroupLeak = threadGroupDescription();
        threadLeak = threadDescription();
        Blackhole.blackhole(threadGroupLeak);
        Blackhole.blackhole(threadLeak);
    }

    private static Node threadDescription()
    {
        Node node = new Node();
        for (int i = 0; i < 1_000_000; i++)
        {
            node.value = new MyThread();
            node.left = new Node();
            node.right = new Node();
            node = node.right;
        }
        return node;
    }

    public final static class MyThread extends Thread {
        public final static String NAME = "My Thread";

        public MyThread() {
            super(NAME);
        }
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
