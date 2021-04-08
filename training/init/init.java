///usr/bin/env jbang "$0" "$@" ; exit $?
// //DEPS <dependency1> <dependency2>

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static java.lang.System.*;

public class init
{
    public static void main(String... args) throws Exception
    {
        AsymmetricEncryption.main();
    }

    static class AsymmetricEncryption
    {
        static final KeyPairGenerator KEY_PAIR_GEN;
        static final Cipher CIPHER;

        static
        {
            out.println("Static block executed");
            try
            {
                KEY_PAIR_GEN = KeyPairGenerator.getInstance("RSA");
                KEY_PAIR_GEN.initialize(1024);

                CIPHER = Cipher.getInstance("RSA");
            }
            catch (NoSuchAlgorithmException | NoSuchPaddingException e)
            {
                throw new RuntimeException(e);
            }
        }

        static void main() throws Exception
        {
            KeyPair keyPair = KEY_PAIR_GEN.generateKeyPair();

            byte[] text = "welcome to the training".getBytes(StandardCharsets.UTF_8);

            // Encrypt with private key
            CIPHER.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
            byte[] encrypted = CIPHER.doFinal(text);

            // Decrypt with public key
            CIPHER.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
            byte[] unencrypted = CIPHER.doFinal(encrypted);

            out.println(new String(unencrypted, StandardCharsets.UTF_8));
        }
    }
}
