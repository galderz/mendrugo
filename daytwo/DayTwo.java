import java.lang.management.ManagementFactory;

public class DayTwo
{
    public static void main(String[] args)
    {
        System.out.println("Day two!");

        System.out.printf(
            "StrictMat.cos(%1$,.2f) = %2$,.12f%n"
            , 0.0
            , StrictMath.cos(0.0)
        );

        System.out.printf(
            "VM name: %s%n"
            , ManagementFactory.getRuntimeMXBean().getName()
        );

        System.out.printf(
            "Inheritable thread local %s"
            , new InheritableThreadLocal<>()
        );
    }
}
