public class SignedTransaction
{
    final String signature;
    final Transaction transaction;

    SignedTransaction(String signature, Transaction transaction)
    {
        this.signature = signature;
        this.transaction = transaction;
    }

    @Override
    public String toString()
    {
        return "SignedTransaction{" +
            "signature='" + signature + '\'' +
            ", transaction=" + transaction +
            '}';
    }

    public static SignedTransaction of(byte[] signature, Transaction transaction)
    {
        return new SignedTransaction(Transactions.toHex(signature), transaction);
    }
}
