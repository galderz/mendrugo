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
        final Object time = time();
        return switch (time) {
            case EvenTime et -> "Even time";
            case UnevenTime ut -> "Uneven time";
            default -> "?";
        };
    }

    Object time() {
        final long time = System.currentTimeMillis();
        if (time % 2 == 0) {
            return new EvenTime(time);
        }

        return new UnevenTime(time);
    }

    static record EvenTime(long time) {}
    static record UnevenTime(long time) {}
}