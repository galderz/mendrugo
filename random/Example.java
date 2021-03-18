import java.util.Arrays;

class Example
{
    static final Randoms randoms;

    static
    {
        randoms = new Randoms();
    }

    public static void main(String[] args)
    {
        System.out.println(
            Arrays.toString(
                randoms.secure.generateSeed(19)
            )
        );
    }
}