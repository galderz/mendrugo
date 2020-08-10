import java.nio.file.Path;
import java.nio.file.Paths;

public class HelloWorld
{
    public static void main(String[] args)
    {
        Path path = Paths.get("/home/g/tmp/japanese/元気かい/graalvm-20.1");
        System.out.println("Path with japanese chars: " + path);

        Path userDir = Paths.get(System.getProperty("user.dir", "."));
        System.out.println("User dir: " + userDir);
    }
}
