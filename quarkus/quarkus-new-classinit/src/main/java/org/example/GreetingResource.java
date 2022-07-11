package org.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        System.out.println("B_InitAtRunTime.b = " + B_InitAtRunTime.b);
        System.out.println("A_InitAtBuildTime.a = " + A_InitAtBuildTime.a);
        return "Hello from RESTEasy Reactive";
    }
}