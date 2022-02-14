///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class tostring
{
    public static void main(String... args)
    {
        final TrainType favouriteTrain = new TrainType("Rio Grande Standard", 87.0);
        System.out.println(favouriteTrain.speed);
    }
}

final class TrainType
{
    final String name;
    final double speed;

    TrainType(String name, double speed)
    {
        this.name = name;
        this.speed = speed;
    }

    @Override
    public String toString()
    {
        return "TrainType{" +
            "name='" + name + '\'' +
            ", speed=" + speed +
            ", serialNumber=" + serialNumber();
    }

    private String serialNumber()
    {
        return SerialNumbers.lookup(name);
    }
}

final class SerialNumbers
{
    static final Map<String, String> SERIAL_NUMBERS = new HashMap<>();

    static String lookup(String name)
    {
        return SERIAL_NUMBERS.computeIfAbsent(name, SerialNumberFactory::create);
    }
}

class SerialNumberFactory
{
    static final Random R = new Random();

    static String create(String name)
    {
        return R.nextInt(name.length() * 100) + "-" + R.nextInt(name.length() * 100);
    }
}
