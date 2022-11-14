package jfr.oldobject;

import jfr.oldobject.gen.Huge;

public class HugeChain
{
    static HugeLink chain;

    public static void main(String[] args)
    {
        chain = new HugeLink();
        for (int i = 0; i < 100; i++) {
            chain.obj = new Huge();
            chain.next = new HugeLink();
            chain = chain.next;
        }

        Blackhole.blackhole(chain);
    }

    static class HugeLink
    {
        Huge obj;
        HugeLink next;

        @Override
        public String toString()
        {
            return "HugeLink{" +
                "obj=" + obj +
                ", next=" + next +
                '}';
        }
    }
}
