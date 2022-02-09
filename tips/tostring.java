///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class tostring
{
    public static void main(String... args)
    {
        final TrainType favouriteTrain = new TrainType(
            "Rio Grande Standard"
            , 87
            , LocalDate.of(1949, 1, 1)
        );

        System.out.println(favouriteTrain.name);
        System.out.println(favouriteTrain.speed);
    }
}

class TrainType
{
    final String name;
    final int speed;
    final LocalDate released;

    TrainType(String name, int speed, LocalDate released) {
        this.name = name;
        this.speed = speed;
        this.released = released;
    }

    @Override
    public String toString()
    {
        return "TrainType{" +
            "name='" + name + '\'' +
            ", speed=" + speed +
            ", released=" + Converters.DATE_CONVERTER.format(released) +
            '}';
    }
}

class Converters
{
    static final Converter<LocalDate> DATE_CONVERTER = new DateConverter();
}

interface Converter<T>
{
    T parse(String s);
    String format(T t);
}

final class DateConverter implements Converter<LocalDate>
{
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate parse(String s)
    {
        return LocalDate.parse(s, DATE_FORMATTER);
    }

    @Override
    public String format(LocalDate localDate)
    {
        return localDate.format(DATE_FORMATTER);
    }
}
