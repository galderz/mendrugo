package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/double")
public class DoubleIncrementResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String doubleIncrement() {
        return String.format("A_InitAtBuildTime.a = %s%nB_InitAtRunTime.b = %s", A_InitAtBuildTime.a, B_InitAtRunTime.b);
    }
}
