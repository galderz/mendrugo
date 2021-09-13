import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

public class Example
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("AWT Graphics, is headless? " + GraphicsEnvironment.isHeadless());
        System.out.println("AWT Toolkit: " + Toolkit.getDefaultToolkit());
    }
}
