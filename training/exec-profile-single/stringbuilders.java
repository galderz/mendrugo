///usr/bin/env jbang "$0" "$@" ; exit $?
// //DEPS <dependency1> <dependency2>

import static java.lang.System.*;

public class stringbuilders
{
    public static void main(String... args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(new char[1_000_000]);

        do {
            sb.append(12345);
            sb.delete(0, 5);
        } while (Thread.currentThread().isAlive());

        System.out.println(sb);  // never happens
    }
}
