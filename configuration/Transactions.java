import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

final class Transactions
{
    static void sign() throws Exception
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);

        final KeyPair aliceKeyPair = keyPairGenerator.generateKeyPair();
        final KeyPair bobKeyPair = keyPairGenerator.generateKeyPair();

        final Transaction transaction = Transaction.of(
            aliceKeyPair.getPublic().getEncoded()
            , bobKeyPair.getPublic().getEncoded()
            , 10
        );

        Signature signatureAlgorithm = Signature.getInstance("SHA256WithRSA");
        signatureAlgorithm.initSign(aliceKeyPair.getPrivate());
        signatureAlgorithm.update(transaction.data().getBytes(StandardCharsets.UTF_8));

        final SignedTransaction signedTransaction = SignedTransaction.of(signatureAlgorithm.sign(), transaction);
        System.out.println(signedTransaction);
    }

    static String toHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
