package org.acme.client;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.acme.common.Elephant;
import org.acme.common.MammalFamily;
import org.acme.common.Whale;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@QuarkusMain
public class ClientMain implements QuarkusApplication
{
    @RestClient
    MammalRestClient client;

    @Override
    public int run(String... args) throws Exception
    {
        System.out.println("Client main");
        client.send(new MammalFamily(List.of(
            new Whale(30.0, "white")
            , new Elephant(10, "africa")
        )));
        return 0;
    }
}
