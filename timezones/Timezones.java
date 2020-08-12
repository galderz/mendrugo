import java.util.Date;
import java.util.TimeZone;

public class Timezones
{
    public static void main(String[] args)
    {
        System.out.println(String.format(
            "%tc", new Date()
        ));

        final var tz = TimeZone.getDefault();
        System.out.println(tz.getDisplayName());
    }
}
