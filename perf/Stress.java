import com.oracle.svm.core.annotate.NeverInline;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class Stress
{
    static final int NUM_ITERATIONS = Integer.getInteger("num.iterations", 500_000);
    static final int NUM_IMPLS = 6;
    static final Random R = new Random();

    public static void main(String[] args)
    {
        final var impls = new Expensive[NUM_IMPLS];
        impls[0] = new Md5();
        impls[1] = new Md5Salt();
        impls[2] = new Sha1();
        impls[3] = new Sha256();
        impls[4] = new Sha384();
        impls[5] = new Sha512();

        IntStream.range(1, NUM_ITERATIONS).forEach(runIteration(impls));
    }

    private static IntConsumer runIteration(Expensive[] impls)
    {
        return i ->
        {
            System.out.printf("Iteration %d%n", i);

            final var index = R.nextInt(NUM_IMPLS);
            String message = null;
            try
            {
                message = impls[index].spend(i);
                if (message.hashCode() == System.nanoTime())
                    System.out.print(" ");
            }
            catch (Throwable t)
            {
                throw new RuntimeException(t);
            }
        };
    }

    static String generateRandomString(int numberOfChars)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfChars; i++)
            sb.append((char) (64 + R.nextInt(26)));
        return sb.toString();
    }

    interface Expensive
    {
        String spend(int i) throws Throwable;
    }

    static class Md5 implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest();

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
    }

    static class Md5Salt implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(getSalt());
            byte[] bytes = md.digest(msg.getBytes(StandardCharsets.UTF_8));

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }

        private static byte[] getSalt() throws Throwable
        {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return salt;
        }
    }

    static class Sha1 implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest();

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
    }

    static class Sha256 implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest();

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
    }

    static class Sha384 implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            md.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest();

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
    }

    static class Sha512 implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest();

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
    }
}