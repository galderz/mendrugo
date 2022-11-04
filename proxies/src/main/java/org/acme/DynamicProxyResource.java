package org.acme;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/proxy")
public class DynamicProxyResource
{
    @GET
    @Path("/security")
    public String security()
    {
        return SecurityDemo.show();
    }
}
