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
        return String.format("B_InitAtRunTime.b = %s%nA_InitAtBuildTime.a = %s%n", B_InitAtRunTime.b, A_InitAtBuildTime.a);
    }
}
