import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Stress
{
    static final int NUM_ITERATIONS = Integer.getInteger("num.iterations", 500_000);
    static final Random R = new Random();

    public static void main(String[] args)
    {
        IntStream.range(0, NUM_ITERATIONS).forEach(runIteration());
    }

    private static IntConsumer runIteration()
    {
        return i ->
        {
            if (i < 1000 || i % 10000 == 0)
                System.out.printf("Iteration %d%n", i);

            final var length = R.nextInt(i + 1) + 1;
            final var text = generateRandomString(length);
            String message = null;
            if (length % 2 == 0)
            {
                final var encoded = cipher1(text, text.length() % 26);
                message = String.format(
                    "hello %s - %tc - %s"
                    , text
                    , new Date()
                    , encoded
                );
            }
            else
            {
                final var encoded = cipher2(text, "this is only a test");
                message = String.format(
                    "hola %s - %tc - %s"
                    , text
                    , new Date()
                    , encoded
                );
            }

            if (message.hashCode() == System.nanoTime())
                System.out.print(" ");
        };
    }

    static String cipher1(String msg, int shift)
    {
        StringBuilder result = new StringBuilder();
        int len = msg.length();
        for (int x = 0; x < len; x++)
        {
            char c = (char) (msg.charAt(x) + shift);
            if (c > 'z')
                result.append((char) (msg.charAt(x) - (26 - shift)));
            else
                result.append((char) (msg.charAt(x) + shift));
        }
        return result.toString();
    }

    static String cipher2(String text, final String key)
    {
        StringBuilder result = new StringBuilder();
        text = text.toUpperCase();
        for (int i = 0, j = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c < 'A' || c > 'Z')
                continue;
            result.append((char) ((c + key.charAt(j) - 2 * 'A') % 26 + 'A'));
            j = ++j % key.length();
        }
        return result.toString().toLowerCase();
    }

    public static String generateRandomString(int numberOfChars)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfChars; i++)
            sb.append((char) (64 + R.nextInt(26)));
        return sb.toString();
    }
}