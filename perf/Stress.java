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
    static final int NUM_IMPLS = 7;
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
        impls[6] = new All();

        IntStream.range(1, NUM_ITERATIONS).forEach(runIteration(impls));
    }

    @NeverInline("On purpose")
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

    @NeverInline("On purpose")
    static String generateRandomString(int numberOfChars)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfChars; i++)
            sb.append((char) (64 + R.nextInt(26)));
        return sb.toString();
    }

    interface Expensive
    {
        @NeverInline("On purpose")
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
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
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

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            md.update(salt);

            byte[] bytes = md.digest(msg.getBytes(StandardCharsets.UTF_8));

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bytes.length; j++)
            {
                sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
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
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
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
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
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

    static class All implements Expensive
    {
        final Random R = new Random();

        @Override
        @NeverInline("On purpose")
        public String spend(int i) throws Throwable
        {
            final String msg = generateRandomString(R.nextInt(i) + 1);

            // Digest
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md5.digest();

            // Hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            String md5Result = sb.toString();

            // Digest
            MessageDigest md5Salt = MessageDigest.getInstance("MD5");

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            md5Salt.update(salt);

            bytes = md5Salt.digest(md5Result.getBytes(StandardCharsets.UTF_8));

            // Hexadecimal
            sb = new StringBuilder();
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            String md5SaltResult = sb.toString();

            // Digest
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(md5SaltResult.getBytes(StandardCharsets.UTF_8));
            bytes = md.digest();

            // Hexadecimal
            sb = new StringBuilder();
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            final var sha1Result = sb.toString();

            // Digest
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(sha1Result.getBytes(StandardCharsets.UTF_8));
            bytes = sha256.digest();

            // Hexadecimal
            sb = new StringBuilder();
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            final var sha256Result = sb.toString();

            // Digest
            MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
            sha384.update(sha256Result.getBytes(StandardCharsets.UTF_8));
            bytes = sha384.digest();

            // Hexadecimal
            sb = new StringBuilder();
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            final var sha384Result = sb.toString();

            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            sha512.update(sha384Result.getBytes(StandardCharsets.UTF_8));
            bytes = sha512.digest();

            // Hexadecimal
            sb = new StringBuilder();
            for (byte aByte : bytes)
            {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            final var sha512Result = sb.toString();

            return sha512Result;
        }
    }
}