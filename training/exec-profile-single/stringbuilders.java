///usr/bin/env jbang "$0" "$@" ; exit $?
// //DEPS <dependency1> <dependency2>

import static java.lang.System.*;

/**
 * Adapted from Andrei Pangin's example:
 * https://github.com/apangin/java-profiling-presentation/blob/master/src/demo1/StringBuilderTest.java
 */
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
