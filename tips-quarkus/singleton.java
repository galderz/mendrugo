//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-resteasy:2.7.1.Final
//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager
//Q:CONFIG quarkus.native.enable-reports=true
//Q:CONFIG quarkus.native.additional-build-args=--initialize-at-run-time=Time

import io.quarkus.runtime.Quarkus;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/singleton/start-time")
@ApplicationScoped
public class singleton
{
    @Inject
    CdiSingleton cdiSingleton;
    
    @GET
    @Path("/cdi")
    public long withCdi()
    {
	return cdiSingleton.startTime();
    }
    
    @GET
    @Path("/enum")
    public long withEnum()
    {
	return EnumSingleton.INSTANCE.startTime();
    }
   
    @GET
    @Path("/static")
    public long withStatic()
    {
	return StaticSingleton.startTime();
    }
 
    public static void main(String[] args)
    {
        Quarkus.run(args);
    }
}

@Singleton
class CdiSingleton
{
    final long startTime = System.currentTimeMillis();
    
    long startTime()
    {
	return startTime;
    }
}

enum EnumSingleton
{
    INSTANCE(System.currentTimeMillis());

    private final long startTime;

    private EnumSingleton(long startTime)
    {
	this.startTime = startTime;
    }

    long startTime()
    {
	return startTime;
    }
}

class StaticSingleton
{
    static final long START_TIME = System.currentTimeMillis();

    static long startTime()
    {
	return START_TIME;
    }
}
