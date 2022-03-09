//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-resteasy:2.7.1.Final
//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager
//Q:CONFIG quarkus.native.enable-reports=true

import io.quarkus.runtime.Quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.util.Random;

@Path("/hello")
@ApplicationScoped
public class impls
{
    @GET
    public String sayHello()
    {
        return "hello";
    }

    public static void main(String[] args)
    {
        Quarkus.run(args);
    }

    @Inject
    ParserService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/parse/{num}")
    public int greeting(@PathParam String num)
    {
        return service.parse(num);
    }

    @ApplicationScoped
    static public class ParserService
    {
        public int parse(String num)
        {
            final long now = System.currentTimeMillis();
            NumberParser numberParser = findNumberParser(now);
            if (numberParser != null)
            {
                return parseInteger(num, numberParser);
            }

            return Integer.MAX_VALUE;
        }

        private NumberParser findNumberParser(long now)
        {
            if (now % 2 == 0)
            {
                return new EvenParser();
            }

            final long last = now % 10;
            if (last == 1)
            {
                return new UnevenParser1();
            }

            if (last == 3)
            {
                return new UnevenParser3();
            }

            if (last == 5)
            {
                return new UnevenParser5();
            }

            if (last == 7)
            {
                return new UnevenParser7();
            }

            if (last == 9)
            {
                return new UnevenParser9();
            }

            return null;
        }

        private int parseInteger(String num, NumberParser parser)
        {
            return parser.integer(num);
        }
    }
}


interface NumberParser
{
    int integer(String num);
}

class EvenParser implements NumberParser
{
    @Override
    public int integer(String num)
    {
        return Integer.parseInt(num);
    }
}

class UnevenParser1 implements NumberParser
{
    @Override
    public int integer(String num)
    {
        return 1;
    }
}

class UnevenParser3 implements NumberParser
{
    @Override
    public int integer(String num)
    {
        return 3;
    }
}

class UnevenParser5 implements NumberParser
{
    @Override
    public int integer(String num)
    {
        return 5;
    }
}

class UnevenParser7 implements NumberParser
{
    @Override
    public int integer(String num)
    {
        return 7;
    }
}

class UnevenParser9 implements NumberParser
{
    @Override
    public int integer(String num)
    {
        return 9;
    }
}

class UnevenParserRandom implements NumberParser
{
    static final Random R = new Random();

    @Override
    public int integer(String num)
    {
        return R.nextInt();
    }
}
