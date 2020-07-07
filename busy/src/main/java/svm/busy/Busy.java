package svm.busy;

public class Busy
{
    public static void main(String[] args) throws InterruptedException
    {
        while (true) {
            System.out.println("Busy...");
            Thread.sleep(1_000);
        }
    }
}
