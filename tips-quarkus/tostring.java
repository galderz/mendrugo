//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-resteasy:2.7.1.Final
//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager
//Q:CONFIG quarkus.native.enable-reports=true

import io.quarkus.runtime.Quarkus;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/train")
@ApplicationScoped
public class tostring
{
    @GET
    public double trainSpeed()
    {
        return service.train().speed;
    }

    public static void main(String[] args)
    {
        Quarkus.run(args);
    }

    @Inject
    TrainService service;

    @ApplicationScoped
    static public class GreetingService
    {
        public String greeting(String name)
	{
            return "hello " + name;
        }
    }

    @ApplicationScoped
    static public class TrainService
    {
	public TrainType train()
	{
	    return new TrainType("Rio Grande Stanard", 87.0);
	}
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

final class SerialNumberFactory
{
    static final Random R = new Random();

    static String create(String name)
    {
        return R.nextInt(name.length() * 100) + "-" + R.nextInt(name.length() * 100);
    }
}
