package org.acme;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/oome")
public class OOMEResource
{
    // int size = 10_000_000;
    int size = Short.MAX_VALUE;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String oome()
    {
        final Object[] objects = new Object[size];
        for (int i = 0; i < objects.length; i++)
        {
            objects[i] = new Object[size];
        }

        return objects.toString();
    }
}
