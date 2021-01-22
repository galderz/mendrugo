public class Transaction
{
    final String from;
    final String to;
    final int amount;

    private Transaction(String from, String to, int amount)
    {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public String data()
    {
        return from + to + amount;
    }

    @Override
    public String toString()
    {
        return "Transaction{" +
            "from='" + from + '\'' +
            ", to='" + to + '\'' +
            ", amount=" + amount +
            '}';
    }

    public static Transaction of(byte[] from, byte[] to, int amount)
    {
        return new Transaction(Transactions.toHex(from), Transactions.toHex(to), amount);
    }
}
