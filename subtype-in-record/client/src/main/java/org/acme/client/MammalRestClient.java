package org.acme.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.common.Mammal;
import org.acme.common.MammalFamily;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/mammals")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "mammal")
public interface MammalRestClient
{
    @POST
    String send(MammalFamily family);
}
