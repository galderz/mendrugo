import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Timezones
{
    public static void main(String[] args)
    {
        printStacktraces(Timezones::displayTimezoneName);

        // Trying to replicate:
        // https://github.com/quarkusio/quarkus/issues/11524
        printStacktraces(Timezones::defaultTimeFormat);
        printStacktraces(Timezones::englishTimeFormat);
        printStacktraces(Timezones::rootTimeFormat);

        printStacktraces(() -> parseTimeAndTimezone(Locale.ENGLISH));
        printStacktraces(() -> parseTimeAndTimezone(Locale.ROOT));
        printStacktraces(() -> parseTimeAndTimezone(Locale.CANADA_FRENCH));
    }

    private static void parseTimeAndTimezone(Locale locale)
    {
        String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss.SSSz"};

        SimpleDateFormat format = new SimpleDateFormat(patterns[0], locale);
        ParsePosition pos = new ParsePosition(0);
        String s = "2019-08-26T17:38:20.917+02:00";

        if (s.endsWith("Z")) {
            s = s.substring(0, s.length() - 1) + "GMT-00:00";
        }

        for (final String pattern : patterns) {
            format.applyPattern(pattern);
            format.setLenient(true);
            pos.setIndex(0);

            Date date = format.parse(s, pos);

            if (date != null) {
                System.out.println("Parsed time and timezone:");
                System.out.println(date.getTime());
            }

//            if (date != null && pos.getIndex() == s.length()) {
//                System.out.println("Parsed time and timezone:");
//                System.out.println(date.getTime());
//            }
        }
    }

    static void rootTimeFormat()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z zzzz", Locale.ROOT);
        System.out.println("ROOT time parsing: " + sdf.format(cal.getTime()));
    }

    static void englishTimeFormat()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z zzzz", Locale.ENGLISH);
        System.out.println("English time parsing: " + sdf.format(cal.getTime()));
    }

    static void defaultTimeFormat()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z zzzz");
        System.out.println("Default time parsing: " + sdf.format(cal.getTime()));
    }

    static void displayTimezoneName()
    {
        System.out.printf("%tc%n", new Date());

        final var tz = TimeZone.getDefault();
        System.out.println("Display timezone name: " + tz.getDisplayName());
    }

    static void printStacktraces(Runnable r)
    {
        try
        {
            r.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
