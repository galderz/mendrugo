package epoll.c;

import io.netty.channel.epoll.Native;

public class Main
{
    public static void main(String[] args)
    {
        System.out.printf("Kernel version %s%n", Native.KERNEL_VERSION);
    }
}
