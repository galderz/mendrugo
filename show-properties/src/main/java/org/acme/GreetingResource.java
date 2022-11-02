package org.acme;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        System.out.println("java.runtime.version=" + System.getProperty("java.runtime.version"));
        System.out.println("java.vm.vendor=" + System.getProperty("java.vm.vendor"));
        System.out.println("java.runtime.name=" + System.getProperty("java.runtime.name"));

        return "Hello from RESTEasy Reactive";
    }
}