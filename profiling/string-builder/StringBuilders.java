// https://github.com/apangin/codeone2019-java-profiling/blob/master/src/demo1/StringBuilderTest.java
public class StringBuilders
{
    public static void main(String[] args)
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
