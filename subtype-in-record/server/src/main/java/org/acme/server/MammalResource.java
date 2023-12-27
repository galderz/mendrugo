package org.acme.server;

import io.quarkus.logging.Log;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.acme.common.Mammal;
import org.acme.common.MammalFamily;

@Path("/mammals")
public class MammalResource
{
    @POST
    public String add(MammalFamily family)
    {
        Log.infof("Received mammal family: %n%s", family);
        return "Ok";
    }
}
