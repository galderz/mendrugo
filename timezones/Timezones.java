import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Timezones
{
    public static void main(String[] args)
    {
        displayTimezoneName();
        parseTimeAndTimezone();
    }

    private static void displayTimezoneName()
    {
        System.out.printf("%tc%n", new Date());

        final var tz = TimeZone.getDefault();
        System.out.println("Display timezone name:");
        System.out.println(tz.getDisplayName());
    }

    // Trying to replicate:
    // https://github.com/quarkusio/quarkus/issues/11524
    private static void parseTimeAndTimezone()
    {
        String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss.SSSz"};

        SimpleDateFormat format = new SimpleDateFormat(patterns[0], Locale.ENGLISH);
        ParsePosition pos = new ParsePosition(0);
        String s = "ISODate(\"2019-08-26T17:38:20.917+02:00\")";

        if (s.endsWith("Z")) {
            s = s.substring(0, s.length() - 1) + "GMT-00:00";
        }

        for (final String pattern : patterns) {
            format.applyPattern(pattern);
            format.setLenient(true);
            pos.setIndex(0);

            Date date = format.parse(s, pos);

            if (date != null && pos.getIndex() == s.length()) {
                System.out.println("Parsed time and timezone:");
                System.out.println(date.getTime());
            }
        }
    }
}
