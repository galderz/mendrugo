import java.util.*;
import java.util.stream.*;

public class Example
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Args: " + Arrays.toString(args));
        System.out.println();

        System.out.println("Sample returns: " + new Example().numberOfArgs(args));
        System.out.println();

        System.out.println("Example declared methods:");
        System.out.println(Stream.of(Example.class.getDeclaredMethods()).map(Object::toString).collect(Collectors.joining("\n")));
    }

    public int numberOfArgs(String[] args) {
        return args.length;
    }
}
