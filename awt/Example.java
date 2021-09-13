import java.awt.GraphicsEnvironment;

public class Example
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("AWT Graphics, is headless? " + GraphicsEnvironment.isHeadless());
    }
}
