package jfr.oldobject;

public class ThreadDescription
{
    static Node leak;

    public static void main(String[] args)
    {
        leak = threadDescription();
        Blackhole.blackhole(leak);
    }

    private static Node threadDescription()
    {
        Node node = new Node();
        for (int i = 0; i < 10_000_000; i++)
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

//        // Allocate array to trigger sampling code path for interpreter / c1
//        byte[] bytes = new byte[10];
    }
}
