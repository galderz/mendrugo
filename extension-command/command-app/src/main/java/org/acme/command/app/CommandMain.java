package org.acme.command.app;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class CommandMain implements QuarkusApplication
{
    @Override
    public int run(String... args)
    {
        System.out.println("Hello " + args[0]);
        return 0;
    }
}
